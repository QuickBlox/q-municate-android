package com.quickblox.q_municate.ui.activities.others;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.views.TouchImageView;
import com.quickblox.q_municate.utils.ToastUtils;

import butterknife.Bind;

public class PreviewImageActivity extends BaseLoggableActivity {

    public static final String EXTRA_IMAGE_URL = "image";

    private static final int IMAGE_MAX_ZOOM = 4;

    @Bind(R.id.image_touchimageview)
    TouchImageView imageTouchImageView;

    public static void start(Context context, String url) {
        Intent intent = new Intent(context, PreviewImageActivity.class);
        intent.putExtra(EXTRA_IMAGE_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_preview_image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        initTouchImageView();
        displayImage();
    }

    private void initFields() {
        title = getString(R.string.preview_image_title);
    }

    private void displayImage() {
        showActionBarProgress();
        String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            ToastUtils.shortToast(R.string.preview_image_error);
                            hideActionBarProgress();
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            hideActionBarProgress();
                            return false;
                        }
                    })
                    .into(imageTouchImageView);
        }
    }

    private void initTouchImageView() {
        imageTouchImageView.setMaxZoom(IMAGE_MAX_ZOOM);
    }
}