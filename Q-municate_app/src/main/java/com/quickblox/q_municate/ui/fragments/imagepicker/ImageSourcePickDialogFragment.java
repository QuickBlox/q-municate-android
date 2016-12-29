package com.quickblox.q_municate.ui.fragments.imagepicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.activities.location.MapsActivity;
import com.quickblox.q_municate.utils.image.ImageUtils;

public class ImageSourcePickDialogFragment extends DialogFragment {

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA = 1;
    private static final int POSITION_LOCATION = 2;

    private OnAttachSourcePickedListener onAttachSourcePickedListener;

    public static void show(FragmentManager fragmentManager, OnAttachSourcePickedListener onAttachSourcePickedListener) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setOnAttachSourcePickedListener(onAttachSourcePickedListener);
        fragment.show(fragmentManager, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(R.string.dlg_choose_image_from);
        builder.items(R.array.dlg_image_pick);
        builder.itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog materialDialog, View view, int i,
                    CharSequence charSequence) {
                switch (i) {
                    case POSITION_GALLERY:
                        onAttachSourcePickedListener.onAttachSourcePicked(AttachSource.GALLERY);
                        break;
                    case POSITION_CAMERA:
                        onAttachSourcePickedListener.onAttachSourcePicked(AttachSource.CAMERA);
                        break;
                    case POSITION_LOCATION:
                        Log.d("ImageSourcePick","POSITION_LOCATION");
                        onAttachSourcePickedListener.onAttachSourcePicked(AttachSource.LOCATION);
                        break;
                }
            }
        });

        MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    public void setOnAttachSourcePickedListener(OnAttachSourcePickedListener onAttachSourcePickedListener) {
        this.onAttachSourcePickedListener = onAttachSourcePickedListener;
    }

    public enum AttachSource {
        GALLERY,
        CAMERA,
        LOCATION
    }

    public interface OnAttachSourcePickedListener {

        void onAttachSourcePicked(AttachSource source);
    }

    public static class LoggableActivityAttachSourcePickedListener implements OnAttachSourcePickedListener {

        private Activity activity;
        private Fragment fragment;

        public LoggableActivityAttachSourcePickedListener(Activity activity) {
            this.activity = activity;
        }

        public LoggableActivityAttachSourcePickedListener(Fragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onAttachSourcePicked(AttachSource source) {
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
                        Log.d("ImageSourcePick","START DIALOG");
                        Intent intent = new Intent(fragment.getContext(), MapsActivity.class);
                        fragment.getContext().startActivity(intent);
                    }
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