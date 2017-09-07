package com.quickblox.q_municate.ui.fragments.imagepicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.utils.DialogsUtils;
import com.quickblox.q_municate.utils.helpers.SystemPermissionHelper;
import com.quickblox.q_municate.utils.image.ImageUtils;

import java.util.ArrayList;
import java.util.Arrays;

public class ImageSourcePickDialogFragment extends DialogFragment {
    private static final String TAG = ImageSourcePickDialogFragment.class.getSimpleName();
    private static final long DELAY_PERMISSIONS_RESULT_ACTIONS = 300;

    private static final int POSITION_GALLERY = 0;
    private static final int POSITION_CAMERA_PHOTO = 1;
    private static final int POSITION_CAMERA_VIDEO = 2;
    private static final int POSITION_LOCATION = 3;
    private static SystemPermissionHelper systemPermissionHelper;

    private OnImageSourcePickedListener onImageSourcePickedListener;
    protected Handler handler = new Handler();

    public ImageSourcePickDialogFragment() {
        systemPermissionHelper = new SystemPermissionHelper(this);
    }

    public static void show(FragmentManager fragmentManager, Fragment imagePickHelperFragment) {
        ImageSourcePickDialogFragment fragment = new ImageSourcePickDialogFragment();
        fragment.setArguments(imagePickHelperFragment.getArguments());
        fragment.setOnImageSourcePickedListener(new ImageSourcePickDialogFragment.LoggableActivityImageSourcePickedListener(imagePickHelperFragment));
        fragment.show(fragmentManager, ImageSourcePickDialogFragment.class.getSimpleName());
    }

    @Override
    public void show(FragmentManager fragmentManager, String tag) {
        fragmentManager.popBackStack();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.add(this, tag);
        fragmentTransaction.commit();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.title(R.string.dlg_choose_media_from);
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
                        if (systemPermissionHelper.isAllPermissionsGrantedForSaveFile()) {
                            onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY);
                        } else {
                            systemPermissionHelper.requestPermissionsForSaveFile();
                        }
                        break;
                    case POSITION_CAMERA_PHOTO:
                        if (systemPermissionHelper.isCameraPermissionGranted()) {
                            onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA_PHOTO);
                        } else {
                            systemPermissionHelper.requestPermissionsTakePhoto();
                        }
                        break;
                    case POSITION_CAMERA_VIDEO:
                        if (systemPermissionHelper.isCameraPermissionGranted()) {
                            onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA_VIDEO);
                        } else {
                            systemPermissionHelper.requestPermissionsTakeVideo();
                        }
                        break;
                    case POSITION_LOCATION:
                        onImageSourcePickedListener.onImageSourcePicked(ImageSource.LOCATION);
                        break;
                }
            }
        });

        MaterialDialog dialog = builder.build();
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode ==  android.view.KeyEvent.KEYCODE_BACK)) {
                    if(getFragmentManager() != null) {
                        getFragmentManager().popBackStack();
                    }
                }
                return false;
            }
        });

        return dialog;
    }

    public void setOnImageSourcePickedListener(OnImageSourcePickedListener onImageSourcePickedListener) {
        this.onImageSourcePickedListener = onImageSourcePickedListener;
    }

    private void showPermissionSettingsDialog(int permissionNameId) {
        DialogsUtils.showOpenAppSettingsDialog(
                getFragmentManager(),
                getString(R.string.dlg_need_permission, getString(R.string.app_name), getString(permissionNameId)),
                new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        SystemPermissionHelper.openSystemSettings(getContext());
                    }
                });
    }

    public enum ImageSource {
        GALLERY,
        CAMERA_PHOTO,
        CAMERA_VIDEO,
        LOCATION
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //postDelayed() is temp fix before fixing this bug https://code.google.com/p/android/issues/detail?id=190966
        //on Android 7+ can use without delay
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                switch(requestCode) {
                    case (SystemPermissionHelper.PERMISSIONS_FOR_TAKE_PHOTO_REQUEST):
                        if (systemPermissionHelper.isCameraPermissionGranted()) {
                            onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA_PHOTO);
                        } else {
                            showPermissionSettingsDialog(R.string.dlg_permission_camera);
                        }
                        break;
                    case (SystemPermissionHelper.PERMISSIONS_FOR_VIDEO_RECORD_REQUEST):
                        if (systemPermissionHelper.isCameraPermissionGranted()) {
                            onImageSourcePickedListener.onImageSourcePicked(ImageSource.CAMERA_VIDEO);
                        } else {
                            showPermissionSettingsDialog(R.string.dlg_permission_camera);
                        }
                        break;
                    case (SystemPermissionHelper.PERMISSIONS_FOR_SAVE_FILE_REQUEST):
                        if (systemPermissionHelper.isAllPermissionsGrantedForSaveFile()) {
                            onImageSourcePickedListener.onImageSourcePicked(ImageSource.GALLERY);
                        } else {
                            showPermissionSettingsDialog(R.string.dlg_permission_storage);
                        }
                        break;
                }

                getFragmentManager().popBackStack();
            }
        }, DELAY_PERMISSIONS_RESULT_ACTIONS);
    }

    public interface OnImageSourcePickedListener {

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
                        ImageUtils.startMediaPicker(fragment);
                    } else {
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startMediaPicker(activity);
                    }
                    break;
                case CAMERA_PHOTO:
                    if (fragment != null) {
                        Activity activity = fragment.getActivity();
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startCameraPhotoForResult(fragment);
                    } else {
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startCameraPhotoForResult(activity);
                    }
                    break;
                case CAMERA_VIDEO:
                    if (fragment != null) {
                        Activity activity = fragment.getActivity();
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startCameraVideoForResult(fragment);
                    } else {
                        setupActivityToBeNonLoggable(activity);
                        ImageUtils.startCameraVideoForResult(activity);
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