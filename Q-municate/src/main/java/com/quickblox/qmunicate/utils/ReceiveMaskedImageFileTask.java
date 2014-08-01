package com.quickblox.qmunicate.utils;

import android.graphics.Bitmap;

import com.quickblox.q_municate.core.concurrency.BaseAsyncTask;
import com.quickblox.q_municate.utils.ImageHelper;

public class ReceiveMaskedImageFileTask extends BaseAsyncTask {

    private ReceiveMaskedBitmapListener receiveMaskedBitmapListener;

    public ReceiveMaskedImageFileTask(ReceiveMaskedBitmapListener receiveMaskedBitmapListener) {
        this.receiveMaskedBitmapListener = receiveMaskedBitmapListener;
    }

    @Override
    public void onResult(Object maskedImageBitmap) {
        receiveMaskedBitmapListener.onMaskedImageBitmapReceived((Bitmap) maskedImageBitmap);
    }

    @Override
    public void onException(Exception e) {
    }

    @Override
    public Object performInBackground(Object[] params) throws Exception {
        Bitmap maskedImageBitmap;
        Bitmap backgroundBitmap;

        ImageHelper imageHelper = (ImageHelper) params[0];
        int resourcesId = (Integer) params[1];
        Bitmap loadedImageBitmap = (Bitmap) params[2];

        backgroundBitmap = imageHelper.decodeBackgroundBubbleImage(resourcesId);
        maskedImageBitmap = imageHelper.generateMask(backgroundBitmap, loadedImageBitmap);

        return maskedImageBitmap;
    }
}