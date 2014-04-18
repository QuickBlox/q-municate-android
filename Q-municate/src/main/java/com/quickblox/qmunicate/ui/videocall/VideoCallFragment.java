package com.quickblox.qmunicate.ui.videocall;

import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.mediacall.OutgoingCallFragment;

import org.webrtc.MediaConstraints;

public class VideoCallFragment extends OutgoingCallFragment {

    @Override
    protected int getContentView() {
        return R.layout.activity_video_call;
    }

    @Override
    protected MediaConstraints getMediaConstraints() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(WebRTC.RECEIVE_AUDIO,
                WebRTC.TRUE_FLAG));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(WebRTC.RECEIVE_VIDEO,
                WebRTC.TRUE_FLAG));
        return sdpMediaConstraints;
    }
}
