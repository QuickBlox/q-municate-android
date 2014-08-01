package com.quickblox.q_municate.utils;

import android.graphics.Bitmap;

import com.quickblox.q_municate.core.concurrency.BaseAsyncTask;

import java.io.File;
import java.io.IOException;

public class ReceiveImageFileTask extends BaseAsyncTask {

    private ReceiveFileListener listener;

    public ReceiveImageFileTask(ReceiveFileListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResult(Object object) {
        if (object instanceof File) {
            listener.onCachedImageFileReceived((File) object);
        } else if (object instanceof String) {
            listener.onAbsolutePathExtFileReceived((String) object);
        }
    }

    @Override
    public void onException(Exception e) {
    }

    @Override
    public Object performInBackground(Object[] params) throws Exception {
        File imageFile;
        String absolutePath;
        ImageHelper imageHelper = (ImageHelper) params[0];
        Bitmap bitmap = (Bitmap) params[1];
        boolean isGettingFile = (Boolean) params[2];

        try {
            if (isGettingFile) {
                imageFile = imageHelper.getFileFromImageView(bitmap);
                return imageFile;
            } else {
                absolutePath = imageHelper.getAbsolutePathByBitmap(bitmap);
                return absolutePath;
            }
        } catch (IOException e) {
            onException(e);
        }

        return null;
    }
}