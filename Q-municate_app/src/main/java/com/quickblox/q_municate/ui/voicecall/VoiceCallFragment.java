package com.quickblox.q_municate.ui.voicecall;

import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.mediacall.OutgoingCallFragment;
import com.quickblox.q_municate.ui.mediacall.TimeUpdater;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate_core.utils.ConstsCore;

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
            ((TextView) rootView.findViewById(R.id.name_textview)).setText(opponent.getFullName());
        }
        timeTextView = (TextView) rootView.findViewById(R.id.timerTextView);
        rootView.findViewById(R.id.muteDynamicButton).setOnClickListener(this);
        if (updater != null) {
            updater.setTextView(timeTextView);
        }
        RoundedImageView avatarView = (RoundedImageView) rootView.findViewById(R.id.avatar_imageview);
        avatarView.setOval(true);
        if(!TextUtils.isEmpty(opponent.getAvatarUrl())){
            ImageLoader.getInstance().displayImage(opponent.getAvatarUrl(),
                    avatarView, Consts.UIL_USER_AVATAR_DISPLAY_OPTIONS);
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
        if (videoChat != null) {
            videoChat.muteSound(!videoChat.isSoundMute());
        }
    }

    private void startTimer(TextView textView) {

        handler = new Handler();
        updater = new TimeUpdater(textView, handler);
        handler.postDelayed(updater, ConstsCore.SECOND);
    }

    private void stopTimer() {
        if (handler != null && updater != null) {
            handler.removeCallbacks(updater);
        }
    }
}
