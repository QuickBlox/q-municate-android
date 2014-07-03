package com.quickblox.qmunicate.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBSignaling;
import com.quickblox.module.chat.exceptions.QBChatException;
import com.quickblox.module.chat.listeners.QBSignalingManagerListener;
import com.quickblox.module.videochat_webrtc.QBSignalingChannel;
import com.quickblox.module.videochat_webrtc.VideoSenderChannel;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.module.videochat_webrtc.model.CallConfig;
import com.quickblox.module.videochat_webrtc.model.ConnectionConfig;
import com.quickblox.module.videochat_webrtc.signaling.SIGNAL_STATE;
import com.quickblox.module.videochat_webrtc.utils.SignalingListenerImpl;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.FriendUtils;

public class QBVideoChatHelper extends BaseHelper {

    private final Lo lo = new Lo(this);
    private VideoSenderChannel signalingChannel;

    private QBSignalingChannel.SignalingListener signalingListener;
    private QBChatService chatService;
    private Class<? extends Activity> activityClass;

    public QBVideoChatHelper(Context context) {
        super(context);
    }

    public VideoSenderChannel getSignalingChannel() {
        return signalingChannel;
    }

    public void init(QBChatService chatService,
            Class<? extends Activity> activityClass) {
        this.chatService = chatService;
        this.activityClass = activityClass;
        lo.g("init videochat");
        signalingListener = new VideoSignalingListener();
        this.chatService.getSignalingManager().addSignalingManagerListener(
                new SignalingManagerListener());
    }

    private class SignalingManagerListener implements QBSignalingManagerListener {

        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                signalingChannel = new VideoSenderChannel(qbSignaling);
                signalingChannel.addSignalingListener(signalingListener);
            }
        }
    }

    public VideoSenderChannel makeSignalingChannel(int participantId) {
        QBSignaling signaling = QBChatService.getInstance().getSignalingManager().createSignaling(
                participantId, null);
        signalingChannel = new VideoSenderChannel(signaling);
        return signalingChannel;
    }

    private class VideoSignalingListener extends SignalingListenerImpl {

        @Override
        public void onError(SIGNAL_STATE state, QBChatException e) {
            lo.g("error while establishing connection" + e.getLocalizedMessage());
        }

        @Override
        public void onCall(ConnectionConfig connectionConfig) {
            CallConfig callConfig = (CallConfig) connectionConfig;
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(
                    callConfig.getSessionDescription());
            lo.g("onCall" + callConfig.getCallStreamType().toString());
            Intent intent = new Intent(context, activityClass);
            intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.INCOMING);
            intent.putExtra(WebRTC.PLATFORM_EXTENSION, callConfig.getDevicePlatform());
            intent.putExtra(WebRTC.ORIENTATION_EXTENSION, callConfig.getDeviceOrientation());
            intent.putExtra(Consts.CALL_TYPE_EXTRA, callConfig.getCallStreamType());
            intent.putExtra(WebRTC.SESSION_ID_EXTENSION, callConfig.getConnectionSession());
            Friend friend = FriendUtils.createFriend(callConfig.getFromUser());
            intent.putExtra(Consts.EXTRA_FRIEND, friend);
            intent.putExtra(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        }
    }
}
