package com.example.kcalmera;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraActivity extends Activity {
    private final static String TAG = "SimpleCamera";
    private Size mPreviewSize;

    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private CameraManager manager;
    private StreamConfigurationMap map;

    private List<Surface> outputSurfaces= new ArrayList<Surface>(2);
    private Surface surface;
    private ImageReader reader;
    private String cameraId;
    private boolean cap;

    private DisplayMetrics displayMetrics = new DisplayMetrics();
    int DSI_height;
    int DSI_width;
    int Trans_height;
    int Trans_width;
    private Size mChoosedPreview;
    int mWidth;
    int mHeight;
    private Button ConfirmButton;
    private Button PictureButton;

    private String setOfResults;
    public static String[] Food;

    private static final int MAX_PREVIEW_WIDTH = 1920;
    private static final int MAX_PREVIEW_HEIGHT = 1080;
    private int mSensorOrientation;
    private boolean semaphore;

    final int MY_PERMISSIONS_REQUEST_CAMERA = 1001;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }


    private static int choosePreviewRealSizeIndex(Size[] choices, Size maxPreviewSize){
        int index=-1;
        int diff = 1000000000;//large value
        int sum;
        for(int i=0;i<choices.length;i++)
        {
            //반대로 뺴줘야 된다.
            sum = Math.abs(maxPreviewSize.getHeight() - choices[i].getWidth()) + Math.abs(maxPreviewSize.getWidth() - choices[i].getHeight());
            if(diff > sum)
            {
                diff = sum;
                index = i;
            }
        }
        return index;
    }



/*
    private void rotateImage(Bitmap bitmap){
        ExifInterface exifInterface = null;
        try{
            exifInterface = new ExifInterface(mImageFileLocation);
        } catch(IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();
        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap,0,0, bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        mPhotoCapturedImageView.setImageBitmap(rotatedBitmap);
    }
*/

    /**
     * this function find maximum preview size available in current device
     * */
    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth, int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        /**aspectRatio is maximum resolution that camera sensor support at taking a picture
         * but we don't use this var as original purpose
         * maxWidth and maxHeight is upper limit
         */
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        /**
         * choices is set of outputSize that current device support when using Texture view
         * process iteration to get set of bigEnough and set of notBIgEnough
         */
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }
/*
    private Size getOptimalPreviewSize(Size[] sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

            for(int i=0;i<sizes.length;i++){
            double ratio = (double) sizes[i].getWidth() / sizes[i].getHeight();
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(sizes[i].getHeight() - targetHeight) < minDiff) {
                optimalSize = sizes[i];
                minDiff = Math.abs(sizes[i].getHeight() - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for(int i=0;i<sizes.length;i++){
                if (Math.abs(sizes[i].getHeight() - targetHeight) < minDiff) {
                    optimalSize = sizes[i];
                    minDiff = Math.abs(sizes[i].getHeight() - targetHeight);
                }
            }
        }
        return optimalSize;
    }
*/
/*
    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for(Size option : mapSizes) {
            if(width > height) {
                if(option.getWidth() > width &&
                        option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else {
                if(option.getWidth() > height &&
                        option.getHeight() > width) {
                    collectorSizes.add(option);
                }
            }
        }
        if(collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return mapSizes[0];
    }
*/

    /**
     * this function maximize textureView size with respect to device maximum width
     * Result of function must have same ratio with preview ratio to realize reality ratio
     * @param ResolutionWidth  is width of preview
     * @param ResolutionHeight is height of preview
     */
    private void setAspectRatioTextureView(int ResolutionWidth , int ResolutionHeight )
    {
        //DSI_width is device maximum width
        //DSI_height is device maximum height
        Log.d(TAG, "TextureView RW: "+ResolutionWidth+ " DSI_W: "+ DSI_width + " RH: "+ ResolutionHeight + " DSI_H: "+ DSI_height);
        if(ResolutionWidth > ResolutionHeight){
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionWidth)/ResolutionHeight);

            updateTextureViewSize(newWidth,newHeight);

        }else {
            int newWidth = DSI_width;
            int newHeight = ((DSI_width * ResolutionHeight)/ResolutionWidth);
            updateTextureViewSize(newWidth,newHeight);
        }

    }

    /**
     * adjust view size
     * @param viewWidth
     * @param viewHeight
     */
    private void updateTextureViewSize(int viewWidth, int viewHeight) {
        Log.d(TAG, "TextureView Width : " + viewWidth + " TextureView Height : " + viewHeight);
        // view size on screen
        Trans_width = viewWidth;
        Trans_height = viewHeight;

        Log.e(TAG,"viewWidth: "+viewWidth + " viewHeight: "+viewHeight);
        mTextureView.setLayoutParams(new FrameLayout.LayoutParams(viewWidth, viewHeight));
    }
    /*
        private static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight)
        {
            byte [] yuv = new byte[imageWidth*imageHeight*3/2];
            // Rotate the Y luma
            int i = 0;
            for(int x = 0;x < imageWidth;x++)
            {
                for(int y = imageHeight-1;y >= 0;y--)
                {
                    yuv[i] = data[y*imageWidth+x];
                    i++;
                }
            }
            // Rotate the U and V color components
            i = imageWidth*imageHeight*3/2-1;
            for(int x = imageWidth-1;x > 0;x=x-2)
            {
                for(int y = 0;y < imageHeight/2;y++)
                {
                    yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+x];
                    i--;
                    yuv[i] = data[(imageWidth*imageHeight)+(y*imageWidth)+(x-1)];
                    i--;
                }
            }
            return yuv;
        }
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);
        mTextureView = findViewById(R.id.preViewScreen);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        ConfirmButton = findViewById(R.id.confirmButton);
        PictureButton = findViewById(R.id.pictureButton);
        //create and set texture view but not yet be create actually
        /*
        mTextureView = new TextureView(this);
        mTextureView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        setContentView(mTextureView);
         */
        //get device screen size
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        DSI_height = displayMetrics.heightPixels;
        DSI_width = displayMetrics.widthPixels;
        Log.e(TAG, "DSI_width="+DSI_width+", DSI_height="+DSI_height);

        /**outputSurfaces에다가 받을 surface를 다 넣는데 일단 Session을 만들때
         mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback()
         onConfigue함수가 호출되면 Session이 만들어지는건데 이렇게 되서 만들어지는 순간 이미 sessio이 bind되서 중간에 바꾸면 안되는 듯 한다.
         새로 Session을 만들지 않는 이상 request를 할 때 묶는 surface도 동일해야만 하는 듯 하고
         mTextureView.getSurfaceTexture()을 하면 onCreate시엔 textureView가 아직 안만들어져서 이게 만들어지고 해야 오류가 안나는 듯 하다.
         마지막으로 시도해본게
         Device 생성시
         CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback()
         이거의 onOpened에 Session을 만드는 것을 넣어봤는데 error가 났다. 이유는 찾진 못했다..
         **/

        //create CameraManager
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        setOfResults = "";
        semaphore = false;

        //when you touch screen, then picture is taken

        ConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.e(TAG, "ConfirmButton clicked");

                if (semaphore == false){
                    Intent intent = new Intent();
                intent.putExtra("INPUT_TEXT", setOfResults);
                setResult(RESULT_OK, intent);
                Food = setOfResults.split("/");
                setOfResults = "";
                semaphore = false;
                finish();
                 }
            }
        }); //버튼에 OnClickListener를 지정(OnClickListener)


        PictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Log.e(TAG, "TextureView clicked");
                //Toast.makeText(CameraActivity.this, "찰칵!!", Toast.LENGTH_SHORT).show();

                if(semaphore == false)
                    takePicture();
            }
        });
    }

    /**
     * observe whether textureview is created
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener(){
        @Override
        /**
         * when textureview is created, this method is automatically called at first
         */
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) { // 텍스쳐뷰가 생성되자마자 크기?
            Log.e(TAG, "onSurfaceTextureAvailable is called, width= "+width+", height= "+height);
            openCamera(width, height);
        }

        @Override
        /**
         * when textureview size is changed, automatically be called.
         */
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {// 변환 전 view의 width height ?
            Log.e(TAG,"onSurfaceTextureSizeChanged is called, "+"width: "+width + " height: "+height);

            configureTransform(Trans_width,Trans_height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //Log.e(TAG, "onSurfaceTextureUpdated");
        }
    };

    /**
     * when permmision is requested, check whether permmisson is confirmed
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
/*
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "승인이 허가되어 있습니다.", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, "아직 승인받지 않았습니다.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
*/
    /**
     * prepare before opening the cameraDevice
     * @param width is initial width of textureview
     * @param height is initial height of textureview
     */
    private void openCamera(int width, int height) {
        try {
            //choose camera sensor and get information about it
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            //map has various information of outputsize that camera sensor can support
            //outputsize means resolution (ex, 1920 x 1080)
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //this line bring maximum outputsize of surfacetexture that sensor can support
            //mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            //get the information that whether screen is rotated
            int displayRotation = this.getWindowManager().getDefaultDisplay().getRotation();
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            boolean swappedDimensions = false;
            switch (displayRotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_180:
                    if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                        swappedDimensions = true;
                    }
                    break;
                case Surface.ROTATION_90:
                case Surface.ROTATION_270:
                    if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                        swappedDimensions = true;
                    }
                    break;
                default:
                    Log.e(TAG, "Display rotation is invalid: " + displayRotation);
            }

            //get screen size of device
            Point displaySize = new Point();
            this.getWindowManager().getDefaultDisplay().getSize(displaySize);//getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            //if device is rotated, modify the following variables
            if (swappedDimensions) {
                rotatedPreviewWidth = height;
                rotatedPreviewHeight = width;
                maxPreviewWidth = displaySize.y;
                maxPreviewHeight = displaySize.x;
            }

            // limit maxPreviewSize to max recommended by camera2 api
            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH;
            }
            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT;
            }

            //get largest outputsize that sensor can support when using jpeg format
            //we don't use but i left this line to use chooseOptimalsize function
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());

            //get optimal preview size appropriate to current device spec
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth, maxPreviewHeight, largest);

            /*for(int i=0;i<30;i++)
            {
                   mPreviewSize = map.getOutputSizes(ImageFormat.JPEG)[i];
                //mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[i];
                //Log.d("resolution","\n"+(i+1)+" "+(float)mPreviewSize.getWidth()/mPreviewSize.getHeight()+"\n");
                                                               // + (float)mPreviewSize.getWidth()/mPreviewSize.getHeight()+"\n"
                                                               // + "w: "+ (float)mPreviewSize.getWidth()+" h: "+mPreviewSize.getHeight());
             //   Log.d("resolution","\n"+(i+1)+" w: "+ (float)mPreviewSize.getWidth()+" h: "+mPreviewSize.getHeight());
            }*/

            //when we use imagereader, mPreviewSize can not be coincide with real image resolution.
            //So before transformation by imagereader, i transformed mPreviewSize and got real image resolution
            int index = choosePreviewRealSizeIndex(map.getOutputSizes(SurfaceTexture.class),mPreviewSize);
            mChoosedPreview = map.getOutputSizes(SurfaceTexture.class)[index];

            Log.e(TAG,"MaximumPreviewSize Width: "+ mPreviewSize.getWidth() + " Height: "+ mPreviewSize.getHeight() +"\n"
                    + "ChoosedPreviewSize Width: " + mChoosedPreview.getWidth() + " Height: " + mChoosedPreview.getHeight() + "\n");

            /*
            Size[] jpegSizes = null;
            if (map != null) {
                jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
            }
            width = 640;
            height = 480;

            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
              */

            try {
                //preview와 textureview를 맞춰준다.
                setAspectRatioTextureView(mChoosedPreview.getWidth(), mChoosedPreview.getHeight());
            }
            catch(Exception e) {
                Log.e(TAG, e.getMessage());
            }

            Log.e(TAG,"Trans_width: "+Trans_width + " Trans_height: "+Trans_height);

            //화면 회전시 적용됨
            configureTransform(Trans_width,Trans_height);

            //이걸 명시적으로 해야 openCamera 가능
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

            manager.openCamera(cameraId, mStateCallback, null);

            //check camera permmision and external storage
            /*
            int permssionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            int permssionCheck2 = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permssionCheck!= PackageManager.PERMISSION_GRANTED || permssionCheck2!= PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {
                    Toast.makeText(this,"000부분 사용을 위해 카메라 권한이 필요합니다.",Toast.LENGTH_LONG).show();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_CAMERA);;
                    Toast.makeText(this, "000부분 사용을 위해 카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }

            }
            else {//if all of needed permmision is confirmed, create cameraDevice object
                //if cameraDevice is created, mStateCallback is called.
                manager.openCamera(cameraId, mStateCallback, null);
                 }
             */
        } catch (CameraAccessException e) {
            Log.e("ccc",e.getMessage());;
        }
    }

    /**
     * when device is rotated, adjust screen
     * @param width is view width
     * @param height is view height
     */
    private void configureTransform(int width, int height) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }

        //check whether device is rotated
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, width, height);
        float aspect =  (float) mPreviewSize.getWidth() / mPreviewSize.getHeight();
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postScale( 1/aspect, aspect, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        //rotate
        mTextureView.setTransform(matrix);
    }

    /**
     * when CameraDevice is created, automatically called
     */
    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        /**
         * when device is created, called at first
         */
        public void onOpened(CameraDevice camera) {
            //camera is CameraDevice just created
            mCameraDevice = camera;
            //카메라 센서의 사진찍을 때 해상도와 프리뷰시 해상도가 같아야
            //사진 이미지와 프리뷰시 이미지의 초점이 맞춰진다.(실제 프리뷰로 보는 이미지가 사진에 딱 맞게 들어간다.)
            //이미지 포맷을 JPEG으로 해야 회전(왼쪽으로 90도)이 안된다... YUV_888로 했을 땐, 어떻게 해도 고쳐지지 않았다.
            //.JPEG로 하면 속도가 느려진다.
            reader = ImageReader.newInstance(mChoosedPreview.getWidth(),mChoosedPreview.getHeight(), ImageFormat.YUV_420_888, 1);
            mWidth = mChoosedPreview.getWidth(); mHeight = mChoosedPreview.getHeight();

            Log.e(TAG,"ImageReaderSize, Weight: "+ reader.getWidth() + " Height: " + reader.getHeight());

            //camera session에 결과를 받아올 surface들을 추가한다.
            outputSurfaces.add(reader.getSurface());
            surface = new Surface(mTextureView.getSurfaceTexture());
            outputSurfaces.add(surface);

            //session 생성을 시작한다.
            createSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.e(TAG, "onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e(TAG, "onError");
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    protected void createSession() {

        if(null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            if(null == mCameraDevice)
                Log.e(TAG, "startPreview mCameraDevice is null");
            if(!mTextureView.isAvailable())
                Log.e(TAG, "startPreview Textureview is available");
            if(null == mPreviewSize)
                Log.e(TAG, "startPreview mPreviewSize is null");
            Log.e(TAG, "startPreview fail, return");
            return;
        }

        try {
            //outputSurfaces로 결과를 전달할 세션을 생성한다. 바로 callback 함수가 불린다.
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                /**
                 * when session is just created, called
                 */
                public void onConfigured(CameraCaptureSession session) {
                    mPreviewSession = session;

                    //프리뷰 request를 진행한다.
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(CameraActivity.this, "onConfigureFailed", Toast.LENGTH_LONG).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * the function create preview request and send to session
     */
    protected void updatePreview() {

        if(null == mCameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }

        //create request
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.e(TAG,e.getMessage());
        }

        //add target surface
        mPreviewBuilder.addTarget(surface);

        //set camera sensor mode for auto mode
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());


        try {
            //send request
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                //In this requset, this callback method is repeatedly called (too much)
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    try {
                        // mPreviewSession.stopRepeating();
                    }
                    catch(Exception e)
                    {
                    }
                    // updatePreview();
                }

            } ,backgroundHandler);



        } catch (CameraAccessException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    protected void takePicture() {
        semaphore = true;
        if(null == mCameraDevice) {
            Log.e(TAG, "mCameraDevice is null, return");
            return;
        }
        try {
            //create capture request and set auto mode about sensor mode
            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            //create reader listener
            //when imagereader can read image, this method is automatically call
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                //At first, this method is called
                public void onImageAvailable(ImageReader reader) {
                    //bring last image in media..?
                    Image image = reader.acquireLatestImage();

                    ByteArrayOutputStream outputbytes = new ByteArrayOutputStream();

                    ByteBuffer bufferY = image.getPlanes()[0].getBuffer();
                    byte[] data0 = new byte[bufferY.remaining()];
                    bufferY.get(data0);

                    ByteBuffer bufferU = image.getPlanes()[1].getBuffer();
                    byte[] data1 = new byte[bufferU.remaining()];
                    bufferU.get(data1);

                    ByteBuffer bufferV = image.getPlanes()[2].getBuffer();
                    byte[] data2 = new byte[bufferV.remaining()];
                    bufferV.get(data2);

                    try
                    {
                        outputbytes.write(data0);
                        outputbytes.write(data2);
                        outputbytes.write(data1);


                        final YuvImage yuvImage = new YuvImage(outputbytes.toByteArray(), ImageFormat.NV21, image.getWidth(),image.getHeight(), null);
                        ByteArrayOutputStream outBitmap = new ByteArrayOutputStream();

                        yuvImage.compressToJpeg(new Rect(0, 0,image.getWidth(), image.getHeight()), 95, outBitmap);
                        /*
                        File mFile = new File(Environment.getExternalStorageDirectory()+"/DCIM","pic.jpg");
                        FileOutputStream outputfile = null;
                        outputfile = new FileOutputStream(mFile);
                        outputfile.write(outBitmap.toByteArray());
                        */
                        byte[] bitmapdata = outBitmap.toByteArray();
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, true);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

                        Bitmap resizeBitmap = ((MainActivity) MainActivity.mContext).resizeBitmap(rotatedBitmap);


                        File file = new File(Environment.getExternalStorageDirectory()+"/DCIM","pic.jpg");
                        FileOutputStream fos = new FileOutputStream(file);

                        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        resizeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

                        Log.i(TAG, "image saved in" + Environment.getExternalStorageDirectory() + "/DCIM/pic.jpg");

                        ImageClassifier classifier = null;

                        try {
                            classifier = new ImageClassifier((MainActivity)MainActivity.mContext);//Main으로..?
                        } catch (IOException e) {
                            Log.e(TAG, "Failed to initialize an image classifier.");
                        }

                        String textToShow = classifier.classifyFrame(resizeBitmap);

                        String[] tempArray = textToShow.split(" ");
                       // if(Double.parseDouble(tempArray[1]) <  0.3)
                            //Toast.makeText(CameraActivity.this, tempArray[0] + " 등록 실패", Toast.LENGTH_SHORT).show();
                        Toast toast = Toast.makeText(CameraActivity.this, tempArray[0] + " 인식", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER,0,360);
                        toast.show();
                        //else
                            setOfResults = setOfResults.concat(tempArray[0] + "/");
                            MainActivity.check=1;

                        semaphore = false;

                        //TextView Foodview=(TextView) findViewById(R.id.DetectedFood);
                        //Foodview.setText(((MainActivity) MainActivity.mContext).Food);

                        if (classifier != null) {
                            classifier.close();

                        }

                        /*File file = new File(Environment.getExternalStorageDirectory()+"/DCIM","pic.jpg");
                        FileOutputStream fos = new FileOutputStream(file);
                        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);*/
                        Log.i(TAG, "image saved in" + Environment.getExternalStorageDirectory() + "/DCIM/pic.jpg");
                        image.close();
                    }
                    catch(Exception e)
                    {
                        Log.e("aaa","ddd");
                    }


/*
                    if (image != null) {
                        //converting to JPEG or YUV_420
                        byte[] jpegData = ImageUtils.imageToByteArray(image);
                        //write to file (for example ..some_path/frame.jpg)
                        FileManager.writeFrame(Environment.getExternalStorageDirectory()+"/DCIM"+"/pic.jpg", jpegData);
                        image.close();
                    }
*/
                    /*
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                            reader.close();
                        }
                    }
                     */
                }
/*
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                      //  output = new FileOutputStream(file);
                     //   output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
                */

            };

            //create handler
            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            //bind imagereader with readerlistener
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            //create captureListenr
            //when capture request is called, this method is called
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                /*
                when capture is completed, this method is called at first
                 */
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    try {
                        //mPreviewSession.stopRepeating();
                        //mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, null);
                    }
                    catch(Exception e)
                    {
                    }
                    updatePreview();
                }
            };

            int rotation = getWindowManager().getDefaultDisplay().getRotation();

            //JPEG_ORIENTATION이기 때문에 YUV에선 안됐던것
            //방향 설정
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            //send request to session
            try {
                mPreviewSession.capture(captureBuilder.build(), captureListener, backgroudHandler);
            }
            catch (Exception e)
            {
                Log.e(TAG,e.getMessage());
            }
            /*
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        //captureBuilder.addTarget(surface);
                        //session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                        mPreviewSession.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);*/

        } catch (CameraAccessException e) {
            Log.e(TAG,e.getMessage());
        }
    }
    /*
        the class to compare size
        It is used to get mChoosedPreview
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    //don't need?
/*
    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Camera2Test");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String timeStamp = "2019-12-07";
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }*/

}

