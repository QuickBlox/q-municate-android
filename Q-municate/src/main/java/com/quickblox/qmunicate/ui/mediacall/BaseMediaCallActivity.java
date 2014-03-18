package com.quickblox.qmunicate.ui.mediacall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.videochat_webrtc.ISignalingChannel;
import com.quickblox.module.videochat_webrtc.QBVideoChat;
import com.quickblox.module.videochat_webrtc.SignalingChannel;
import com.quickblox.module.videochat_webrtc.VideoStreamsView;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.gcm.NotificationHelper;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.utils.Consts;

import org.webrtc.MediaConstraints;

/**
 * Created by vadim on 11.03.14.
 */
public abstract class BaseMediaCallActivity extends BaseActivity implements ISignalingChannel.MessageObserver {

    private static final String TAG = BaseMediaCallActivity.class.getSimpleName();
    private QBVideoChat qbVideoChat;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive"+intent.getExtras().toString());
            if(NotificationHelper.ACTION_VIDEO_CALL.equals(intent.getAction())){
                Toast.makeText(BaseMediaCallActivity.this, "mesg from", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initChat(getIntent().getIntExtra(Consts.FRIEND_FIELD_FRIEND_ID, Consts.NOT_INITIALIZED_VALUE));
    }

    private void initChat(int userID) {
        MediaConstraints mediaConstraints = getMediaConstraints();
        SignalingChannel signalingChannel = new SignalingChannel(QBChatService.getInstance().getPrivateChatInstance());
        VideoStreamsView videoView = _findViewById(R.id.ownVideoScreenImageView);
        qbVideoChat = new QBVideoChat(this, mediaConstraints, signalingChannel, videoView);
        signalingChannel.addMessageObserver(this);
        if(userID != -1) {
            qbVideoChat.call(userID);
            initCallUI();
        }
    }

    private void initCallUI() {

    }

    abstract public MediaConstraints getMediaConstraints();

    @Override
    public void onPause() {
        super.onPause();
        if (qbVideoChat != null) {
            qbVideoChat.onActivityPause();
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (qbVideoChat != null) {
            qbVideoChat.onActivityResume();
        }
        IntentFilter filter = new IntentFilter(
                NotificationHelper.ACTION_VIDEO_CALL);
        registerReceiver(broadcastReceiver, filter);
    }


    @Override
    protected void onDestroy() {
        disconnectAndExit();
        super.onDestroy();
    }

    private void disconnectAndExit() {
        if (qbVideoChat != null) {
            qbVideoChat.dispose();
        }
    }


    @Override
    public void onCall(String s) {

    }

    @Override
    public void onAccepted(String s) {

    }

    @Override
    public void onStop(String s) {

    }

    @Override
    public void onRejected(String s) {

    }
}
