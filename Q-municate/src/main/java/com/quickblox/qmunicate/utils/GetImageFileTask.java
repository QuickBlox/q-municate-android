package com.quickblox.qmunicate.utils;

import android.graphics.Bitmap;

import com.quickblox.qmunicate.core.concurrency.BaseAsyncTask;

import java.io.File;
import java.io.IOException;

public class GetImageFileTask extends BaseAsyncTask {

    private OnGetFileListener listener;

    public GetImageFileTask(OnGetFileListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResult(Object object) {
        if (object instanceof File) {
            listener.onGotCachedFile((File) object);
        } else if (object instanceof String) {
            listener.onGotAbsolutePathCreatedFile((String) object);
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