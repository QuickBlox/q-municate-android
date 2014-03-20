package com.quickblox.qmunicate.ui.utils;

import android.widget.ImageView;

import com.quickblox.qmunicate.core.concurrency.BaseAsyncTask;

import java.io.File;
import java.io.IOException;

public class GetImageFileTask extends BaseAsyncTask {
    GettingImageFileListener listener;

    public GetImageFileTask(GettingImageFileListener listener) {
        this.listener = listener;
    }

    @Override
    public void onResult(Object imageFile) {
        listener.onGotImageFile((File) imageFile);
    }

    @Override
    public void onException(Exception e) {
    }

    @Override
    public Object performInBackground(Object[] params) throws Exception {
        File imageFile = null;
        ImageHelper imageHelper = (ImageHelper) params[0];
        ImageView imageView = (ImageView) params[1];

        try {
            imageFile = imageHelper.getFileFromImageView(imageView);
        } catch (IOException e) {
            onException(e);
        }

        return imageFile;
    }
}