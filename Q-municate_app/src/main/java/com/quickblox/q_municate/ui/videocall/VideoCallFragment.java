package com.quickblox.q_municate.ui.videocall;

import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.mediacall.OutgoingCallFragment;

import org.webrtc.VideoRendererGui;

public class VideoCallFragment extends OutgoingCallFragment {

    public static final String TAG = "LCYCLE" + VideoCallFragment.class.getSimpleName();
    private GLSurfaceView videoView;
    private ImageView imgMyCameraOff;
    private ToggleButton cameraOffButton;
    private ImageButton switchCameraButton;
    private boolean isVideoEnabled = true;

    @Override
    protected int getContentView() {
        return R.layout.activity_video_call;
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

        Log.d("CALL_INTEGRATION", "init chat video view");
        VideoRendererGui.setView(videoView, new Runnable() {
            @Override
            public void run() {
            }
        });

        Log.d("CALL_INTEGRATION","addView to videoChatHelper");
        videoChatHelper.addVideoView(videoView);

    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.cameraOffButton:
                toggleCamera();
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

            int height = displaymetrics.heightPixels;
            int width = displaymetrics.widthPixels;

            ViewGroup.LayoutParams layoutParams = imgMyCameraOff.getLayoutParams();

//            ViewGroup.LayoutParams videoViewLayoutParams = videoView.getLayoutParams();
            int videoViewHeight = videoView.getHeight();
            int videoViewWidth = videoView.getWidth();

            Log.d(TAG, "height - videoViewHeight " + (height - videoViewHeight) + " width-videoViewWidth " + (width-videoViewWidth) + "");



            layoutParams.height = (int)Math.ceil(((/*height*/videoViewHeight / 100) * 32));
            layoutParams.width = (int)Math.ceil(((/*width*/ videoViewWidth / 100) * 33));


            imgMyCameraOff.setLayoutParams(layoutParams);

            Log.d(TAG, "Width is: " + imgMyCameraOff.getLayoutParams().width + " height is:" + imgMyCameraOff.getLayoutParams().height);

            if (isVideoEnabled){
                outgoingCallFragmentInterface.offCam();
                isVideoEnabled = false;
                switchCameraButton.setVisibility(View.INVISIBLE);
                imgMyCameraOff.setVisibility(View.VISIBLE);
                Log.d(TAG, "Camera disabled");
            } else {
                outgoingCallFragmentInterface.onCam();
                isVideoEnabled = true;
                switchCameraButton.setVisibility(View.VISIBLE);
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
