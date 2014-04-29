package com.quickblox.qmunicate.ui.voicecall;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.mediacall.OutgoingCallFragment;
import com.quickblox.qmunicate.ui.mediacall.TimeUpdater;
import com.quickblox.qmunicate.utils.Consts;

public class VoiceCallFragment extends OutgoingCallFragment {

    private Handler handler;
    private TimeUpdater updater;
    private TextView timeTextView;

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.muteDynamicButton:
                muteSound();
                break;
            default:
                break;
        }
    }

    @Override
    protected void postInit(View rootView) {
        if (opponent != null) {
            ((TextView) rootView.findViewById(R.id.nameTextView)).setText(opponent.getFullName());
        }
        timeTextView = (TextView) rootView.findViewById(R.id.timerTextView);
        rootView.findViewById(R.id.muteDynamicButton).setOnClickListener(this);
        if (updater != null) {
            updater.setTextView(timeTextView);
        }
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
    protected void onConnectionClosed() {
        super.onConnectionClosed();
        stopTimer();
    }

    private void muteSound() {
        if (qbVideoChat != null) {
            qbVideoChat.muteSound(!qbVideoChat.isSoundMute());
        }
    }

    private void startTimer(TextView textView) {

        handler = new Handler();
        updater = new TimeUpdater(textView, handler);
        handler.postDelayed(updater, Consts.SECOND);
    }

    private void stopTimer() {
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }
}
