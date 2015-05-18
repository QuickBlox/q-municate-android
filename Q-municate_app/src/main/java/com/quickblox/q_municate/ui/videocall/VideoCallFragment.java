package com.quickblox.q_municate.ui.videocall;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.mediacall.CallActivity;
import com.quickblox.q_municate.ui.mediacall.OutgoingCallFragment;

import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

public class VideoCallFragment extends OutgoingCallFragment {

    public static final String TAG = VideoCallFragment.class.getSimpleName();
    private GLSurfaceView videoView;
    private ImageView imgMyCameraOff;
    private ToggleButton cameraOffButton;
    private ImageButton switchCameraButton;
    private boolean isVideoEnabled = true;
    private boolean isCameraEnabled = true;

    @Override
    protected int getContentView() {
        return R.layout.activity_video_call;
    }

    @Override
    public void onPause() {
        if (isVideoEnabled) {
            isVideoEnabled = true;
            toggleCamera();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isCameraEnabled && !isVideoEnabled){
            toggleCamera();
        }
    }

    @Override
    protected void initUI(View rootView) {

        Log.d(TAG, "initUI()");

        super.initUI(rootView);

        videoView = (GLSurfaceView) rootView.findViewById(R.id.ownVideoScreenImageView);

        imgMyCameraOff = (ImageView) rootView.findViewById(R.id.imgMyCameraOff);

        cameraOffButton = (ToggleButton) rootView.findViewById(R.id.cameraOffButton);
        cameraOffButton.setOnClickListener(this);

        switchCameraButton = (ImageButton) rootView.findViewById(R.id.switchCameraButton);
        switchCameraButton.setOnClickListener(this);

//        Log.d(TAG, "layoutParams = " + layoutParams.width/6 + " " + videoView.getWidth() /*+ " marginLayoutParams.width = " + marginLayoutParams.width + " marginLayoutParams.width = " + marginLayoutParams.width*/);

        Log.d("CALL_INTEGRATION", "init chat video view");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // SET VIEW FROM UI
                VideoRendererGui.setView(videoView, new Runnable() {
                    @Override
                    public void run() {

                    }
                });

                // CREATE RENDERERS ON UI
                ((CallActivity) getActivity()).setVideoView();
            }
        });
//        Log.d("CALL_INTEGRATION","addView to videoChatHelper");
//        videoChatHelper.addVideoView(videoView);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cameraOffButton:
                toggleCamera();
                if (isCameraEnabled){
                    isCameraEnabled = false;
                } else {
                    isCameraEnabled = true;
                }
                break;
            case R.id.switchCameraButton:
                switchCamera();
                break;
            default:
                break;
        }
    }

    private void toggleCamera() {
        if (outgoingCallFragmentInterface != null){
            DisplayMetrics displaymetrics = new DisplayMetrics();
            displaymetrics.setToDefaults();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
            ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

//            ViewGroup.LayoutParams videoViewLayoutParams = videoView.getLayoutParams();
            int videoViewHeight = videoView.getHeight();
            int videoViewWidth = videoView.getWidth();

//        Log.d(TAG, "height - videoViewHeight " + (height - videoViewHeight) + " width-videoViewWidth " + (width - videoViewWidth) + "");



            layoutParams.height = (int)Math.ceil(((/*height*/videoViewHeight / 100) * 33));
            layoutParams.width = (int)Math.ceil(((/*width*/ videoViewWidth / 100) * 33));
            imgMyCameraOff.setLayoutParams(layoutParams);

            if (isVideoEnabled){
                outgoingCallFragmentInterface.offCam();
                isVideoEnabled = false;
                switchCameraButton.setVisibility(View.INVISIBLE);
                cameraOffButton.setChecked(true);
                imgMyCameraOff.setVisibility(View.VISIBLE);
                Log.d(TAG, "Camera disabled");
            } else {
                outgoingCallFragmentInterface.onCam();
                isVideoEnabled = true;
                switchCameraButton.setVisibility(View.VISIBLE);
                cameraOffButton.setChecked(false);
                imgMyCameraOff.setVisibility(View.INVISIBLE);
                Log.d(TAG, "Camera enabled");
            }
        }
    }

    private void switchCamera(){
        if (outgoingCallFragmentInterface != null) {
            outgoingCallFragmentInterface.switchCam();
            Log.d(TAG, "Camera switched!");
        }
    }
}
