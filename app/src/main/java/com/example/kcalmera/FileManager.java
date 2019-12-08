package com.example.kcalmera;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public final class FileManager {
    public static void writeFrame(String fileName, byte[] data) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
            bos.write(data);
            bos.flush();
            bos.close();
            Log.e("ccccc", "" + fileName );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}