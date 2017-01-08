package com.quickblox.q_municate.ui.fragments.imagepicker;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.utils.image.ImageUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class ImageSourcePickDialogFragment extends DialogFragment {

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;
    private static final int POSITION_LOCATION = 2;

    private OnImageSourcePickedListener onImageSourcePickedListener;

    public static void show(FragmentManager fragmentManager, Fragment imagePickHelperFragment) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setArguments(imagePickHelperFragment.getArguments());
        fragment.setOnImageSourcePickedListener(new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(imagePickHelperFragment));
        fragment.show(fragmentManager, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(R.string.dlg_choose_image_from);
        String[] imagePickArray = getResources().getStringArray(R.array.dlg_image_pick);
        ArrayList<String> imagePickList = new ArrayList<>(Arrays.asList(imagePickArray));
        if (getArguments().getInt("requestCode") != ImageUtils.IMAGE_LOCATION_REQUEST_CODE) {
            imagePickList.remove(2);
        }
        builder.items(imagePickList.toArray(new String[imagePickList.size()]));
        builder.itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int i,
                                    CharSequence charSequence) {
                switch (i) {
                    case POSITION_GALLERY:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY);
                        break;
                    case POSITION_CAMERA:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA);
                        break;
                    case POSITION_LOCATION:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.LOCATION);
                        break;
                }
            }
        });

        MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    public void setOnImageSourcePickedListener(OnImageSourcePickedListener onImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener;
    }

    public static enum ImageSource {
        GALLERY,
        CAMERA,
        LOCATION
    }

    public static interface OnImageSourcePickedListener {

        void onImageSourcePicked(ImageSource source);
    }

    public static class LoggableActivityImageSourcePickedListener implements OnImageSourcePickedListener {

        private Activity activity;
        private Fragment fragment;

        public LoggableActivityImageSourcePickedListener(Activity activity) {
            this.activity = activity;
        }

        public LoggableActivityImageSourcePickedListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onImageSourcePicked(ImageSource source) {
            switch (source) {
                case GALLERY:
                    if (fragment != null) {
                        Activity activity = fragment.getActivity();
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startImagePicker(fragment);
                    } else {
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startImagePicker(activity);
                    }
                    break;
                case CAMERA:
                    if (fragment != null) {
                        Activity activity = fragment.getActivity();
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startCameraForResult(fragment);
                    } else {
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startCameraForResult(activity);
                    }
                    break;
                case LOCATION:
                    if (fragment != null) {
                        Activity activity = fragment.getActivity();
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startMapForResult(fragment);
                    } else {
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startMapForResult(activity);
                    }
                    break;
            }
        }

        private void setupActivityToBeNonLoggable(Activity activity) {
            if (activity instanceof BaseLoggableActivity) {
                BaseLoggableActivity loggableActivity = (BaseLoggableActivity) activity;
                loggableActivity.canPerformLogout.set(false);
            }
        }
    }
}