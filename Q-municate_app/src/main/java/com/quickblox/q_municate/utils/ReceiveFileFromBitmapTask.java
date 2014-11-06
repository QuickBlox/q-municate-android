package com.quickblox.q_municate.utils;

import android.graphics.Bitmap;

import com.quickblox.q_municate_core.core.concurrency.BaseAsyncTask;

import java.io.File;
import java.io.IOException;

public class ReceiveFileFromBitmapTask extends BaseAsyncTask {

    private ReceiveFileListener receiveFileListener;

    public ReceiveFileFromBitmapTask(ReceiveFileListener receiveFileListener) {
        this.receiveFileListener = receiveFileListener;
    }

    @Override
    public void onResult(Object object) {
        if (object instanceof File) {
            receiveFileListener.onCachedImageFileReceived((File) object);
        } else if (object instanceof String) {
            receiveFileListener.onAbsolutePathExtFileReceived((String) object);
        }
    }

    @Override
    public void onException(Exception e) {
    }

    @Override
    public Object performInBackground(Object[] params) throws Exception {
        File imageFile;
        String absolutePath;
        ImageUtils imageUtils = (ImageUtils) params[0];
        Bitmap bitmap = (Bitmap) params[1];
        boolean isGettingFile = (Boolean) params[2];

        try {
            if (isGettingFile) {
                imageFile = imageUtils.getFileFromBitmap(bitmap);
                return imageFile;
            } else {
                absolutePath = imageUtils.getAbsolutePathByBitmap(bitmap);
                return absolutePath;
            }
        } catch (IOException e) {
            onException(e);
        }

        return null;
    }

    public interface ReceiveFileListener {

        public void onCachedImageFileReceived(File imageFile);

        public void onAbsolutePathExtFileReceived(String absolutePath);
    }
}