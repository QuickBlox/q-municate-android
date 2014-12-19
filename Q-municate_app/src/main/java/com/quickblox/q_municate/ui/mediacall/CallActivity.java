package com.quickblox.q_municate.ui.mediacall;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBVideoChannel;
import com.quickblox.videochat.webrtc.listener.QBVideoChatWebRTCSignalingListenerImpl;
import com.quickblox.videochat.webrtc.model.ConnectionConfig;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.communication.SessionDescriptionWrapper;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.push.QBSendPushCommand;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseLogeableActivity;
import com.quickblox.q_municate.ui.media.MediaPlayerManager;
import com.quickblox.q_municate.ui.videocall.VideoCallFragment;
import com.quickblox.q_municate.ui.voicecall.VoiceCallFragment;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.videochat.webrtc.signaling.QBSignalingChannel;
import com.quickblox.videochat.webrtc.signaling.SignalingIgnoreFilter;

public class CallActivity extends BaseLogeableActivity implements IncomingCallFragment.IncomingCallClickListener, OutgoingCallFragment.OutgoingCallListener {

    private static final String TAG = CallActivity.class.getSimpleName();

    private User opponent;
    private ConstsCore.CALL_DIRECTION_TYPE call_direction_type;
    private SessionDescriptionWrapper sessionDescriptionWrapper;
    private com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM call_type;
    private QBVideoChannel signalingChannel;
    private MediaPlayerManager mediaPlayer;
    private String sessionId;
    private QBSignalingChannel.PLATFORM remotePlatform;
    private QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION deviceOrientation;
    private ChatMessageHandler messageHandler;
    private QBVideoChatHelper videoChatHelper;
    private ConnectionConfig currentConfig;

    public static void start(Context context, User friend, com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType) {
        Log.i (TAG,  "Friend.isOnline() = " + friend.isOnline());
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra(ConstsCore.EXTRA_FRIEND, friend);
        intent.putExtra(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, ConstsCore.CALL_DIRECTION_TYPE.OUTGOING);
        intent.putExtra(ConstsCore.CALL_TYPE_EXTRA, callType);
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
        addAction(QBServiceConsts.SEND_PUSH_MESSAGES_FAIL_ACTION, failAction);
    }

    @Override
    public void onConnectedToService(QBService service) {
        if (ConstsCore.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
            videoChatHelper = (QBVideoChatHelper) service.getHelper(QBService.VIDEO_CHAT_HELPER);
            signalingChannel = videoChatHelper.getSignalingChannel(opponent.getUserId());
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
        call_direction_type = (ConstsCore.CALL_DIRECTION_TYPE) extras.getSerializable(
                ConstsCore.CALL_DIRECTION_TYPE_EXTRA);
        call_type = (com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM) extras.getSerializable(ConstsCore.CALL_TYPE_EXTRA);
        remotePlatform = (QBSignalingChannel.PLATFORM) extras.getSerializable(
                com.quickblox.videochat.webrtc.Consts.PLATFORM_EXTENSION);
        deviceOrientation = (QBSignalingChannel.PLATFORM_DEVICE_ORIENTATION) extras.getSerializable(
                com.quickblox.videochat.webrtc.Consts.ORIENTATION_EXTENSION);
        Log.i(TAG, "call_direction_type=" + call_direction_type);
        Log.i(TAG, "call_type=" + call_type);
        sessionId = extras.getString(com.quickblox.videochat.webrtc.Consts.SESSION_ID_EXTENSION, "");
        opponent = (User) extras.getSerializable(ConstsCore.EXTRA_FRIEND);
        if (call_direction_type != null) {
            if (ConstsCore.CALL_DIRECTION_TYPE.INCOMING.equals(call_direction_type)) {
                sessionDescriptionWrapper = extras.getParcelable(ConstsCore.REMOTE_DESCRIPTION);
                showIncomingFragment();
            } else {
                notifyFriendOnCall(opponent);
                showOutgoingFragment();
            }
        }
        Log.i(TAG, "opponentId=" + opponent);
    }

    private void notifyFriendOnCall(User friend){
        if (!friend.isOnline()) {
            String callMsg = getResources().getString(R.string.dlg_offline_call,
                    AppSession.getSession().getUser().getFullName());
            QBSendPushCommand.start(this, callMsg, friend.getUserId());
        }
    }

    private void showOutgoingFragment() {
        playOutgoingRingtone();
        OutgoingCallFragment outgoingCallFragment = (com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO.equals(
                call_type)) ? new VideoCallFragment() : new VoiceCallFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, call_direction_type);
        bundle.putSerializable(ConstsCore.EXTRA_FRIEND, opponent);
        bundle.putSerializable(ConstsCore.CALL_TYPE_EXTRA, call_type);
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

    private void showOutgoingFragment(SessionDescriptionWrapper sessionDescriptionWrapper, User opponentId,
            com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM callType, String sessionId) {
        Bundle bundle = VideoCallFragment.generateArguments(sessionDescriptionWrapper, opponentId,
                call_direction_type, callType, sessionId, remotePlatform, deviceOrientation);
        OutgoingCallFragment outgoingCallFragment = (com.quickblox.videochat.webrtc.Consts.MEDIA_STREAM.VIDEO.equals(
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

    private class ChatMessageHandler extends QBVideoChatWebRTCSignalingListenerImpl {

        @Override
        public void onStop(ConnectionConfig connectionConfig) {
            currentConfig = connectionConfig;
            onConnectionClosed();
        }
    }
}