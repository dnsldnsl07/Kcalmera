from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

from absl import logging

import argparse
import collections
from datetime import datetime
import hashlib
import os.path
import random
import re
import sys

import numpy as np
import tensorflow as tf
import tensorflow_hub as hub
from tensorflow.contrib import quantize as contrib_quantize


def create_image_lists(image_dir, testing_percentage, validation_percentage):
  """Builds a list of training images from the file system.
  Analyzes the sub folders in the image directory, splits them into stable
  training, testing, and validation sets, and returns a data structure
  describing the lists of images for each label and their paths.
  Args:
    image_dir: String path to a folder containing subfolders of images.
    testing_percentage: Integer percentage of the images to reserve for tests.
    validation_percentage: Integer percentage of images reserved for validation.
  Returns:
    An OrderedDict containing an entry for each label subfolder, with images
    split into training, testing, and validation sets within each label.
    The order of items defines the class indices.
  """
  MAX_NUM_IMAGES_PER_CLASS = 2 ** 27 - 1  # ~134M
  
  if not tf.gfile.Exists(image_dir):
    logging.error("Image directory '" + image_dir + "' not found.")
    return None
  result = collections.OrderedDict()
  sub_dirs = sorted(x[0] for x in tf.gfile.Walk(image_dir))
  # The root directory comes first, so skip it.
  is_root_dir = True
  for sub_dir in sub_dirs:
    if is_root_dir:
      is_root_dir = False
      continue
    extensions = sorted(set(os.path.normcase(ext)  # Smash case on Windows.
                            for ext in ['JPEG', 'JPG', 'jpeg', 'jpg', 'png']))
    file_list = []
    dir_name = os.path.basename(
        # tf.gfile.Walk() returns sub-directory with trailing '/' when it is in
        # Google Cloud Storage, which confuses os.path.basename().
        sub_dir[:-1] if sub_dir.endswith('/') else sub_dir)

    if dir_name == image_dir:
      continue
    logging.info("Looking for images in '%s'",  dir_name)
    for extension in extensions:
      file_glob = os.path.join(image_dir, dir_name, '*.' + extension)
      file_list.extend(tf.gfile.Glob(file_glob))
    if not file_list:
      logging.warning('No files found')
      continue
    if len(file_list) < 20:
      logging.warning(
          'WARNING: Folder has less than 20 images, which may cause issues.')
    elif len(file_list) > MAX_NUM_IMAGES_PER_CLASS:
      logging.warning(
          'WARNING: Folder %s has more than %s images. Some images will '
          'never be selected.', dir_name, MAX_NUM_IMAGES_PER_CLASS)
    label_name = re.sub(r'[^a-z0-9]+', ' ', dir_name.lower())
    training_images = []
    testing_images = []
    validation_images = []
    for file_name in file_list:
      base_name = os.path.basename(file_name)
      # We want to ignore anything after '_nohash_' in the file name when
      # deciding which set to put an image in, the data set creator has a way of
      # grouping photos that are close variations of each other. For example
      # this is used in the plant disease data set to group multiple pictures of
      # the same leaf.
      hash_name = re.sub(r'_nohash_.*$', '', base_name)
      # This looks a bit magical, but we need to decide whether this file should
      # go into the training, testing, or validation sets, and we want to keep
      # existing files in the same set even if more files are subsequently
      # added.
      # To do that, we need a stable way of deciding based on just the file name
      # itself, so we do a hash of that and then use that to generate a
      # probability value that we use to assign it.
      hash_name_hashed = hashlib.sha1(tf.compat.as_bytes(hash_name)).hexdigest()
      percentage_hash = ((int(hash_name_hashed, 16) %
                          (MAX_NUM_IMAGES_PER_CLASS + 1)) *
                         (100.0 / MAX_NUM_IMAGES_PER_CLASS))
      if percentage_hash < validation_percentage:
        validation_images.append(base_name)
      elif percentage_hash < (testing_percentage + validation_percentage):
        testing_images.append(base_name)
      else:
        training_images.append(base_name)
    result[label_name] = {
        'dir': dir_name,
        'training': training_images,
        'testing': testing_images,
        'validation': validation_images,
    }
  return result

def create_module_graph(module_spec):
  """Creates a graph and loads Hub Module into it.
  Args:
    module_spec: the hub.ModuleSpec for the image module being used.
  """
  height, width = hub.get_expected_image_size(module_spec)
  with tf.Graph().as_default() as graph:
    resized_input_tensor = tf.placeholder(tf.float32, [None, height, width, 3])
    m = hub.Module(module_spec)
    final_tensor = m(resized_input_tensor)
  return graph, final_tensor, resized_input_tensor

def add_jpeg_decoding(module_spec):
  """Adds operations that perform JPEG decoding and resizing to the graph..
  Args:
    module_spec: The hub.ModuleSpec for the image module being used.
  Returns:
    Tensors for the node to feed JPEG data into, and the output of the
      preprocessing steps.
  """
  input_height, input_width = hub.get_expected_image_size(module_spec)
  input_depth = hub.get_num_image_channels(module_spec)
  jpeg_data = tf.placeholder(tf.string, name='DecodeJPGInput')
  decoded_image = tf.image.decode_jpeg(jpeg_data, channels=input_depth)
  # Convert from full range of uint8 to range [0,1] of float32.
  decoded_image_as_float = tf.image.convert_image_dtype(decoded_image,
                                                        tf.float32)
  decoded_image_4d = tf.expand_dims(decoded_image_as_float, 0)
  resize_shape = tf.stack([input_height, input_width])
  resize_shape_as_int = tf.cast(resize_shape, dtype=tf.int32)
  resized_image = tf.image.resize_bilinear(decoded_image_4d,
                                           resize_shape_as_int)
  return jpeg_data, resized_image




# main
module_url = "https://tfhub.dev/google/imagenet/mobilenet_v2_140_224/classification/3"
module_spec = hub.load_module_spec(module_url)
MAX_NUM_IMAGES_PER_CLASS = 2 ** 27 - 1  # ~134M
# image_dir = "tf_files/FoodDB"
image_dir = "gdrive/My Drive/Capston2/FoodDB"
label_file_name = "gdrive/My Drive/Capston2/ImageNetLabels.txt"
result_file_name = "gdrive/My Drive/Capston2/answer_and_result.txt"
result_lines = list()

with open(label_file_name, "r") as f:
  mapped_labels = f.readlines()
  mapped_labels = list(map(lambda s: s.strip(), mapped_labels))
  mapped_labels = list(map(lambda s: re.sub(' ', '_', s), mapped_labels))
# create empty file (for removing previous result)
f = open(result_file_name, "w")
f.close()

image_lists = create_image_lists(image_dir, 10, 10)
graph, final_tensor, resized_image_tensor = create_module_graph(module_spec)

with tf.Session(graph=graph) as sess:
  # Initialize all weights: for the module to their pretrained values,
  # and for the newly added retraining layer to random initial values.
  init = tf.global_variables_initializer()
  sess.run(init)

  jpeg_data_tensor, decoded_image_tensor = add_jpeg_decoding(module_spec)

  for label_name, label_lists in image_lists.items():
    testing_list = label_lists['testing']
    for index, unused_base_name in enumerate(testing_list): 
      sub_dir = label_lists['dir']
      image_path = os.path.join(image_dir, sub_dir, unused_base_name)

      if not tf.gfile.Exists(image_path):
        print('File does not exist %s', image_path)
      image_data = tf.gfile.GFile(image_path, 'rb').read()
      try:
        resized_input_values = sess.run(decoded_image_tensor, {jpeg_data_tensor: image_data})
        final_values = sess.run(final_tensor, {resized_image_tensor: resized_input_values})
        result_values = np.squeeze(final_values)
        result_index = np.argmax(result_values)
             
        line = label_name + " " + mapped_labels[result_index] + "\n"
        result_lines.append(line)
        print(line)

      except Exception as e:
        raise RuntimeError('Error during processing file %s (%s)' % (image_path, str(e)))

with open(result_file_name, "w") as f:
  for line in result_lines:
    f.write(line)
