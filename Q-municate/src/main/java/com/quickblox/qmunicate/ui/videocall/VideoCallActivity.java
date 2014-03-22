package com.quickblox.qmunicate.ui.videocall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.mediacall.BaseMediaCallActivity;

import org.webrtc.MediaConstraints;

public class VideoCallActivity extends BaseMediaCallActivity {
    public static void start(Context context) {
        Intent intent = new Intent(context, VideoCallActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        initUI();
    }

    @Override
    public MediaConstraints getMediaConstraints() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveVideo", "true"));
        return sdpMediaConstraints;
    }

    private void initUI() {
        actionBar.hide();
    }

}