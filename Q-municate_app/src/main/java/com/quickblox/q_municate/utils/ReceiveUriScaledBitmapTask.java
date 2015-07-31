package com.quickblox.q_municate.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.ui.chats.GroupDialogDetailsActivity;
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

        DisplayImageOptions displayImageOptions;
        if(receiveUriScaledBitmapListener instanceof GroupDialogDetailsActivity){
            displayImageOptions = Consts.UIL_GROUP_AVATAR_DISPLAY_OPTIONS;
        } else {
            // If async task was called from SignUpActivity or ProfileActivity classes
            displayImageOptions = Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS;
        }

        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(originalUri.toString(), displayImageOptions);
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