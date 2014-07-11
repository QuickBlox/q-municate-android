package com.quickblox.qmunicate.ui.mediacall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.module.videochat_webrtc.VideoSenderChannel;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.module.videochat_webrtc.model.ConnectionConfig;
import com.quickblox.module.videochat_webrtc.signalings.QBSignalingChannel;
import com.quickblox.module.videochat_webrtc.signalings.SignalingIgnoreFilter;
import com.quickblox.module.videochat_webrtc.utils.SignalingListenerImpl;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.push.QBSendPushCommand;
import com.quickblox.qmunicate.qb.helpers.QBVideoChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseLogeableActivity;
import com.quickblox.qmunicate.ui.media.MediaPlayerManager;
import com.quickblox.qmunicate.ui.videocall.VideoCallFragment;
import com.quickblox.qmunicate.ui.voicecall.VoiceCallFragment;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.Utils;

public class CallActivity extends BaseLogeableActivity implements IncomingCallFragment.IncomingCallClickListener, OutgoingCallFragment.OutgoingCallListener {

    private static final String TAG = CallActivity.class.getSimpleName();

    private Friend opponent;
    private Consts.CALL_DIRECTION_TYPE call_direction_type;
    private SessionDescriptionWrapper sessionDescriptionWrapper;
    private WebRTC.MEDIA_STREAM call_type;
    private VideoSenderChannel signalingChannel;
    private MediaPlayerManager mediaPlayer;
    private String sessionId;
    private QBSignalingChannel.PLATFORM remotePlatform;
    private QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation;
    private ChatMessageHandler messageHandler;
    private QBVideoChatHelper videoChatHelper;
    private ConnectionConfig currentConfig;

    public static void start(Context context, Friend friend, WebRTC.MEDIA_STREAM callType) {
        if (!friend.isOnline()) {
            String callMsg = context.getResources().getString(R.string.dlg_offline_call,
                    AppSession.getSession().getUser().getFullName());
            QBSendPushCommand.start(context, callMsg, friend.getId());
        }
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(Consts.EXTRA_FRIEND, friend);
        intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.OUTGOING);
        intent.putExtra(Consts.CALL_TYPE_EXTRA, callType);
        context.startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onAcceptClick() {
        accept();
    }

    @Override
    public void onDenyClick() {
        reject();
    }

    @Override
    public void onConnectionAccepted() {
        cancelPlayer();
    }

    @Override
    public void onConnectionRejected() {
        unregisterListener();
        cancelPlayer();
        finish();
    }

    @Override
    public void onConnectionClosed() {
        if (videoChatHelper != null && currentConfig != null){
            videoChatHelper.closeSignalingChannel(currentConfig);
        }
        unregisterListener();
        finish();
    }

    private void unregisterListener(){
        if (signalingChannel != null && messageHandler != null) {
            signalingChannel.removeSignalingListener(messageHandler);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        canPerformLogout.set(false);
        setContentView(R.layout.activity_main_call);
        actionBar.hide();
        mediaPlayer = App.getInstance().getMediaPlayer();
        parseIntentExtras(getIntent().getExtras());
    }

    @Override
    protected void onConnectedToService() {
        if (Consts.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
            videoChatHelper = (QBVideoChatHelper) service.getHelper(QBService.VIDEO_CHAT_HELPER);
            signalingChannel = videoChatHelper.getSignalingChannel(opponent.getId());
            if (signalingChannel != null) {
                messageHandler = new ChatMessageHandler();
                signalingChannel.addSignalingListener(messageHandler);
                signalingChannel.addSignalingIgnoreFilter(messageHandler, new SignalingIgnoreFilter.Equals(
                        QBSignalingChannel.PacketType.qbvideochat_call));
            } else {
                DialogUtils.showLong(this, getString(R.string.dlg_wrong_signaling));
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        cancelPlayer();
        super.onDestroy();
    }

    private void cancelPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stopPlaying();
        }
    }

    private void reject() {
        if (signalingChannel != null && opponent != null) {
            QBUser userOpponent = Utils.friendToUser(opponent);
            currentConfig = new ConnectionConfig(userOpponent, sessionId);
            signalingChannel.sendReject(currentConfig);
        }
        onConnectionClosed();
    }

    private void accept() {
        cancelPlayer();
        showOutgoingFragment(sessionDescriptionWrapper, opponent, call_type, sessionId);
    }

    private void parseIntentExtras(Bundle extras) {
        call_direction_type = (Consts.CALL_DIRECTION_TYPE) extras.getSerializable(
                Consts.CALL_DIRECTION_TYPE_EXTRA);
        call_type = (WebRTC.MEDIA_STREAM) extras.getSerializable(Consts.CALL_TYPE_EXTRA);
        remotePlatform = (QBSignalingChannel.PLATFORM) extras.getSerializable(WebRTC.PLATFORM_EXTENSION);
        deviceOrientation = (QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION) extras.getSerializable(
                WebRTC.ORIENTATION_EXTENSION);
        Log.i(TAG, "call_direction_type=" + call_direction_type);
        Log.i(TAG, "call_type=" + call_type);
        sessionId = extras.getString(WebRTC.SESSION_ID_EXTENSION, "");
        opponent = (Friend) extras.getSerializable(Consts.EXTRA_FRIEND);
        if (call_direction_type != null) {
            if (Consts.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
                sessionDescriptionWrapper = extras.getParcelable(Consts.REMOTE_DESCRIPTION);
                showIncomingFragment();
            } else {
                showOutgoingFragment();
            }
        }
        Log.i(TAG, "opponentId=" + opponent);
    }

    private void showOutgoingFragment() {
        playOutgoingRingtone();
        OutgoingCallFragment outgoingCallFragment = (WebRTC.MEDIA_STREAM.VIDEO.equals(
                call_type)) ? new VideoCallFragment() : new VoiceCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Consts.CALL_DIRECTION_TYPE_EXTRA, call_direction_type);
        bundle.putSerializable(Consts.EXTRA_FRIEND, opponent);
        bundle.putSerializable(Consts.CALL_TYPE_EXTRA, call_type);
        outgoingCallFragment.setArguments(bundle);
        setCurrentFragment(outgoingCallFragment);
    }

    private void playOutgoingRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.playSound("calling.mp3", true);
        }
    }

    private void playIncomingRingtone() {
        if (mediaPlayer != null) {
            mediaPlayer.playDefaultRingTone();
        }
    }

    private void showOutgoingFragment(SessionDescriptionWrapper sessionDescriptionWrapper, Friend opponentId,
            WebRTC.MEDIA_STREAM callType, String sessionId) {
        Bundle bundle = VideoCallFragment.generateArguments(sessionDescriptionWrapper, opponentId,
                call_direction_type, callType, sessionId, remotePlatform, deviceOrientation);
        OutgoingCallFragment outgoingCallFragment = (WebRTC.MEDIA_STREAM.VIDEO.equals(
                call_type)) ? new VideoCallFragment() : new VoiceCallFragment();
        outgoingCallFragment.setArguments(bundle);
        setCurrentFragment(outgoingCallFragment);
    }

    private void showIncomingFragment() {
        playIncomingRingtone();
        IncomingCallFragment incomingCallFragment = IncomingCallFragment.newInstance(call_type,
                opponent);
        setCurrentFragment(incomingCallFragment);
    }

    private class ChatMessageHandler extends SignalingListenerImpl {

        @Override
        public void onStop(ConnectionConfig connectionConfig) {
            currentConfig = connectionConfig;
            onConnectionClosed();
        }
    }
}