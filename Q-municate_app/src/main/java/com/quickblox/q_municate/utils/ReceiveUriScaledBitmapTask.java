package com.quickblox.q_municate.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.io.File;
import java.io.IOException;

public class ReceiveUriScaledBitmapTask extends AsyncTask<Object, Uri, Uri> {

    private ReceiveUriScaledBitmapListener receiveUriScaledBitmapListener;

    public ReceiveUriScaledBitmapTask(ReceiveUriScaledBitmapListener receiveUriScaledBitmapListener) {
        this.receiveUriScaledBitmapListener = receiveUriScaledBitmapListener;
    }

    @Override
    protected Uri doInBackground(Object[] params) {
        ImageUtils imageUtils = (ImageUtils) params[0];
        Uri originalUri = (Uri) params[1];

        File bitmapFile = null;
        Uri outputUri = null;

        Bitmap bitmap = imageUtils.getBitmap(originalUri);
        Bitmap scaledBitmap = imageUtils.createScaledBitmap(bitmap);

        try {
            bitmapFile = imageUtils.getFileFromBitmap(scaledBitmap);
        } catch (IOException error) {
            ErrorUtils.logError(error);
        }

        if (bitmapFile != null) {
            outputUri = Uri.fromFile(bitmapFile);
        }

        return outputUri;
    }

    @Override
    protected void onPostExecute(Uri uri) {
        receiveUriScaledBitmapListener.onUriScaledBitmapReceived(uri);
    }

    public interface ReceiveUriScaledBitmapListener {

        public void onUriScaledBitmapReceived(Uri uri);
    }
}