package com.quickblox.q_municate.ui.cropper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.edmodo.cropper.CropImageView;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.ImageUtils;

import java.io.File;
import java.io.IOException;

public class ImageCropperActivity extends BaseLogeableActivity {

    public static final int INTENT_RESULT_CODE = 911;
    private static final int DEFAULT_ASPECT_RATIO_VALUES = 5;

    private CropImageView cropImageView;

    private Bitmap defaultImageBitmap;
    private Bitmap croppedImageBitmap;
    private ImageUtils imageUtils;

    public static void start(Activity activity, Uri originalUri) {
        Intent intent = new Intent(activity, ImageCropperActivity.class);
        intent.putExtra(QBServiceConsts.EXTRA_FILE_PATH, originalUri);
        activity.startActivityForResult(intent, INTENT_RESULT_CODE);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        canPerformLogout.set(true);
        initUI();
        initFields();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initFields() {
        imageUtils = new ImageUtils(this);
        croppedImageBitmap = null;
        Uri originalUri = getIntent().getParcelableExtra(QBServiceConsts.EXTRA_FILE_PATH);
        defaultImageBitmap = imageUtils.getBitmap(originalUri);
        cropImageView.setImageBitmap(defaultImageBitmap);
        cropImageView.setAspectRatio(DEFAULT_ASPECT_RATIO_VALUES, DEFAULT_ASPECT_RATIO_VALUES);
    }

    private void initUI() {
        cropImageView = (CropImageView) findViewById(R.id.crop_imageview);
    }

    private void sendFeedbackActivityData() {
        if (croppedImageBitmap != null) {
            new ReceiveFilePathTask().execute();
            showProgress();
        } else {
            setResult(RESULT_CANCELED, new Intent());
        }
    }

    public void cropOnClick(View view) {
        croppedImageBitmap = cropImageView.getCroppedImage();
        sendFeedbackActivityData();
    }

    public void cancelOnClick(View view) {
        finish();
    }

    public void onCompressedBitmapReceived(String byteArray) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(QBServiceConsts.EXTRA_FILE_PATH, byteArray);
        setResult(RESULT_OK, returnIntent);
        hideProgress();
        finish();
    }

    private class ReceiveFilePathTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            byte[] byteArray = ImageUtils.getBytesBitmap(croppedImageBitmap);
            File file = null;

            try {
                file = imageUtils.createFile(byteArray);
            } catch (IOException e) {
                ErrorUtils.logError(e);
            }

            return file != null ? file.getAbsolutePath() : null;
        }

        @Override
        protected void onPostExecute(Object object) {
            onCompressedBitmapReceived(object.toString());
        }
    }
}