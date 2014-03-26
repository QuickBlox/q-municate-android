package com.quickblox.qmunicate.ui.voicecall;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.mediacall.OutgoingCallFragment;
import com.quickblox.qmunicate.ui.mediacall.TimeUpdater;
import com.quickblox.qmunicate.ui.utils.Consts;

import org.webrtc.MediaConstraints;

public class VoiceCallFragment extends OutgoingCallFragment {
    private Handler handler;
    private TimeUpdater updater;
    private TextView timeTextView;

    @Override
    protected void postInit(View rootView) {
        if (opponent != null) {
            ((TextView) rootView.findViewById(R.id.nameTextView)).setText(opponent.getFullName());
        }
        timeTextView = (TextView) rootView.findViewById(R.id.timerTextView);
    }

    @Override
    protected void onConnectionEstablished() {
        super.onConnectionEstablished();
        startTimer(timeTextView);
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_voice_call;
    }

    @Override
    protected MediaConstraints getMediaConstraints() {
        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                WebRTC.RECEIVE_AUDIO, WebRTC.TRUE_FLAG));
        return sdpMediaConstraints;
    }

    @Override
    protected void onConnectionClosed() {
        super.onConnectionClosed();
        stopTimer();
    }

    private void startTimer(TextView textView) {
        handler = new Handler();
        updater = new TimeUpdater(textView,
                handler);
        handler.postDelayed(updater, Consts.SECOND);
    }

    private void stopTimer() {
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }

}
