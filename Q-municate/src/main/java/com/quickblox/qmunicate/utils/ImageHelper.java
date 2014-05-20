package com.quickblox.qmunicate.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {

    public static final int GALLERY_KITKAT_INTENT_CALLED = 2;
    public static final int GALLERY_INTENT_CALLED = 1;

    private static final String TEMP_FILE_NAME = "temp.png";

    private Activity activity;

    public ImageHelper(Activity activity) {
        this.activity = activity;
    }

    public void getImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(intent, GALLERY_INTENT_CALLED);
        } else {
            showKitKatGallery();
        }
    }

    private void showKitKatGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
    }

    public File getFileFromImageView(Bitmap origBitmap) throws IOException {
        int preferredWidth = 300;

        int origWidth = origBitmap.getWidth();
        int origHeight = origBitmap.getHeight();

        int destHeight, destWidth;

        if (origWidth <= preferredWidth || origHeight <= preferredWidth) {
            destWidth = origWidth;
            destHeight = origHeight;
        } else {
            destWidth = 300;
            destHeight = origHeight / (origWidth / destWidth);
        }

        File tempFile = new File(activity.getCacheDir(), TEMP_FILE_NAME);
        tempFile.createNewFile();

        Bitmap bitmap = resizeBitmap(origBitmap, destWidth, destHeight);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
        byte[] bitmapData = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(bitmapData);
        fos.close();

        return tempFile;
    }

    private Bitmap resizeBitmap(Bitmap inputBitmap, int newWidth, int newHeight) {
        return Bitmap.createScaledBitmap(inputBitmap, newWidth, newHeight, true);
    }
}