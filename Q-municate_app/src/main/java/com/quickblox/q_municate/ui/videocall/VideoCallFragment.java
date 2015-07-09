package com.quickblox.q_municate.ui.videocall;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.mediacall.CallActivity;
import com.quickblox.q_municate.ui.mediacall.OutgoingCallFragment;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;

/**
 * Implementation of {@link OutgoingCallFragment} for VIDEO calls representation
 */

public class VideoCallFragment extends OutgoingCallFragment {

    public static final String TAG = VideoCallFragment.class.getSimpleName();
    private ImageView imgMyCameraOff;
    private ToggleButton cameraOffButton;
    private ImageButton switchCameraButton;
    private QBGLVideoView localVideoView;
    private QBGLVideoView remoteVideoView;
    private VideoCallFragment.CameraState cameraState = null;

    @Override
    protected int getContentView() {
        return R.layout.activity_video_call;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraState == CameraState.DISABLED_FROM_USER){
            toggleCamera(false);
        } else if (cameraState == CameraState.ENABLED_FROM_USER || cameraState == CameraState.NONE) {
            toggleCamera(true);
        }
    }

    @Override
    public void onPause() {
        if (!isCallStopped()) {
            toggleCamera(false);
            if (cameraState == null) {
                cameraState = CameraState.NONE;
            }
        }

        super.onPause();
    }

    @Override
    protected void initUI(View rootView) {

        Log.d(TAG, "initUI()");

        localVideoView = (QBGLVideoView) rootView.findViewById(R.id.localVideoView);
        remoteVideoView = (QBGLVideoView) rootView.findViewById(R.id.remoteVideoView);

        ((CallActivity)getActivity()).setLocalVideoView(localVideoView);
        ((CallActivity)getActivity()).setRemoteVideoView(remoteVideoView);

        imgMyCameraOff = (ImageView) rootView.findViewById(R.id.imgMyCameraOff);

        cameraOffButton = (ToggleButton) rootView.findViewById(R.id.cameraOffButton);
        cameraOffButton.setOnClickListener(this);

        switchCameraButton = (ImageButton) rootView.findViewById(R.id.switchCameraButton);
        switchCameraButton.setOnClickListener(this);

        super.initUI(rootView);
    }

    @Override
    public void setActionButtonsEnability(boolean enability) {
        super.setActionButtonsEnability(enability);

        cameraOffButton.setEnabled(enability);
        switchCameraButton.setEnabled(enability);

        // inactivate toggle buttons
        cameraOffButton.setActivated(enability);
        switchCameraButton.setActivated(enability);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cameraOffButton:
                if (cameraState != CameraState.DISABLED_FROM_USER) {
                    toggleCamera(false);
                    cameraState = CameraState.DISABLED_FROM_USER;
                } else {
                    toggleCamera(true);
                    cameraState = CameraState.ENABLED_FROM_USER;
                }
                break;
            case R.id.switchCameraButton:
                switchCamera();
                break;
            default:
                break;
        }
    }

    private void toggleCamera(boolean isCameraEnabled) {
        if (callVideoActionsListener != null){
            ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

            int videoViewHeight = localVideoView.getHeight();
            int videoViewWidth = localVideoView.getWidth();

            layoutParams.height = videoViewHeight;
            layoutParams.width = videoViewWidth;
            imgMyCameraOff.setLayoutParams(layoutParams);

            if (!isCameraEnabled){
                switchCameraButton.setVisibility(View.INVISIBLE);
                cameraOffButton.setChecked(true);
                imgMyCameraOff.setVisibility(View.VISIBLE);
                Log.d(TAG, "Camera disabled");
                callVideoActionsListener.onCam(false);
            } else {
                switchCameraButton.setVisibility(View.VISIBLE);
                cameraOffButton.setChecked(false);
                imgMyCameraOff.setVisibility(View.INVISIBLE);
                Log.d(TAG, "Camera enabled");
                callVideoActionsListener.onCam(true);
            }
        }
    }

    private void switchCamera(){
        if (callVideoActionsListener != null) {
            callVideoActionsListener.switchCam();
            Log.d(TAG, "Camera switched!");
        }
    }

    private enum CameraState {
        NONE, DISABLED_FROM_USER, ENABLED_FROM_USER
    }
}
