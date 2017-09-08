package com.quickblox.q_municate.ui.fragments.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.Pair;

import com.quickblox.q_municate.tasks.GetFilepathFromUriTask;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.listeners.OnImagePickedListener;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.ui.kit.chatmessage.adapter.utils.LocationUtils;


public class ImagePickHelperFragment extends Fragment {

    private static final String ARG_REQUEST_CODE = "requestCode";
    private static final String ARG_PARENT_FRAGMENT = "parentFragment";

    private static final String TAG = ImagePickHelperFragment.class.getSimpleName();

    private OnImagePickedListener listener;

    public ImagePickHelperFragment() {
    }

    public static ImagePickHelperFragment start(Fragment fragment, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);
        args.putString(ARG_PARENT_FRAGMENT, fragment.getClass().getSimpleName());

        return start(fragment.getActivity().getSupportFragmentManager(), args);
    }

    public static ImagePickHelperFragment start(FragmentActivity activity, int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_REQUEST_CODE, requestCode);

        return start(activity.getSupportFragmentManager(), args);
    }

    private static ImagePickHelperFragment start(FragmentManager fm, Bundle args) {
        ImagePickHelperFragment fragment = (ImagePickHelperFragment) fm.findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new ImagePickHelperFragment();
            fm.beginTransaction().add(fragment, TAG).commitAllowingStateLoss();
            fragment.setArguments(args);
        }
        return fragment;
    }

    public static void stop(FragmentManager fm) {
        Fragment fragment = fm.findFragmentByTag(TAG);
        if (fragment != null) {
            fm.beginTransaction().remove(fragment).commitAllowingStateLoss();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isResultFromImagePick(requestCode, resultCode, data)) {
            if (requestCode == ImageUtils.IMAGE_VIDEO_LOCATION_REQUEST_CODE) {
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    double latitude = bundle.getDouble(ConstsCore.EXTRA_LOCATION_LATITUDE);
                    double longitude = bundle.getDouble(ConstsCore.EXTRA_LOCATION_LONGITUDE);
                    String location = LocationUtils.generateLocationJson(new Pair<>(ConstsCore.LATITUDE_PARAM, latitude),
                            new Pair<>(ConstsCore.LONGITUDE_PARAM, longitude));
                    listener.onImagePicked(requestCode, Attachment.Type.LOCATION, location);
                }
            } else {
                if ((requestCode == ImageUtils.CAMERA_PHOTO_REQUEST_CODE || requestCode == ImageUtils.CAMERA_VIDEO_REQUEST_CODE) && (data == null || data.getData() == null)) {
                    // Hacky way to get EXTRA_OUTPUT param to work.
                    // When setting EXTRA_OUTPUT param in the camera intent there is a chance that data will return as null
                    // So we just pass temporary camera file as a data, because RESULT_OK means that photo was written in the file.
                    data = new Intent();
                    data.setData(ImageUtils.getValidUri(ImageUtils.getLastUsedCameraFile(), this.getContext()));
                }
                new GetFilepathFromUriTask(getChildFragmentManager(), listener,
                        getArguments().getInt(ARG_REQUEST_CODE)).execute(data);
            }
        } else {
            stop(getChildFragmentManager());
            if (listener != null) {
                listener.onImagePickClosed(getArguments().getInt(ARG_REQUEST_CODE));
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment fragment = ((BaseActivity) activity).getSupportFragmentManager()
                .findFragmentByTag(getArguments().getString(ARG_PARENT_FRAGMENT));
        if (fragment != null) {
            if (fragment instanceof OnImagePickedListener) {
                listener = (OnImagePickedListener) fragment;
            }
        } else {
            if (activity instanceof OnImagePickedListener) {
                listener = (OnImagePickedListener) activity;
            }
        }

        if (listener == null) {
            throw new IllegalStateException(
                    "Either activity or fragment should implement OnImagePickedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setListener(OnImagePickedListener listener) {
        this.listener = listener;
    }

    private boolean isResultFromImagePick(int requestCode, int resultCode, Intent data) {
        return resultCode == Activity.RESULT_OK && ((requestCode == ImageUtils.CAMERA_PHOTO_REQUEST_CODE || requestCode == ImageUtils.CAMERA_VIDEO_REQUEST_CODE) || (requestCode == ImageUtils.GALLERY_REQUEST_CODE && data != null)
                || (requestCode == ImageUtils.IMAGE_VIDEO_LOCATION_REQUEST_CODE && data != null));
    }
}