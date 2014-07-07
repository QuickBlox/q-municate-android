package com.quickblox.qmunicate.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBSignaling;
import com.quickblox.module.chat.exceptions.QBChatException;
import com.quickblox.module.chat.listeners.QBSignalingManagerListener;
import com.quickblox.module.videochat_webrtc.VideoSenderChannel;
import com.quickblox.module.videochat_webrtc.WebRTC;
import com.quickblox.module.videochat_webrtc.model.CallConfig;
import com.quickblox.module.videochat_webrtc.model.ConnectionConfig;
import com.quickblox.module.videochat_webrtc.signalings.QBSignalingChannel;
import com.quickblox.module.videochat_webrtc.signalings.SignalingIgnoreFilter;
import com.quickblox.module.videochat_webrtc.utils.SignalingListenerImpl;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.FriendUtils;

import java.util.HashMap;
import java.util.Map;

public class QBVideoChatHelper extends BaseHelper {

    private final Lo lo = new Lo(this);

    private QBSignalingChannel.SignalingListener signalingListener;
    private QBChatService chatService;
    private Class<? extends Activity> activityClass;
    private Map<Integer, VideoSenderChannel> activeChannels = new HashMap<Integer, VideoSenderChannel>(3);

    public QBVideoChatHelper(Context context) {
        super(context);
    }

    public VideoSenderChannel getSignalingChannel(int participantId) {
        return activeChannels.get(participantId);
    }

    public void init(QBChatService chatService, Class<? extends Activity> activityClass) {
        this.chatService = chatService;
        this.activityClass = activityClass;
        lo.g("init videochat");
        this.chatService.getSignalingManager().addSignalingManagerListener(new SignalingManagerListener());
    }

    private class SignalingManagerListener implements QBSignalingManagerListener {

        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                if (activeChannels.containsKey(qbSignaling.getParticipant())){
                    return;
                }
                VideoSenderChannel signalingChannel = new VideoSenderChannel(qbSignaling);
                VideoSignalingListener videoSignalingListener = new VideoSignalingListener(
                        qbSignaling.getParticipant());
                signalingChannel.addSignalingListener(videoSignalingListener);
                activeChannels.put(qbSignaling.getParticipant(), signalingChannel);
            }
        }
    }

    public void closeSignalingChannel(int participantId) {
        activeChannels.remove(participantId);
    }

    public VideoSenderChannel makeSignalingChannel(int participantId) {
        QBSignaling signaling = QBChatService.getInstance().getSignalingManager().createSignaling(
                participantId, null);
        VideoSenderChannel signalingChannel = new VideoSenderChannel(signaling);
        activeChannels.put(participantId, signalingChannel);
        return signalingChannel;
    }

    private class VideoSignalingListener extends SignalingListenerImpl {

        private int participantId;

        VideoSignalingListener(int participantId) {
            this.participantId = participantId;
        }

        @Override
        public void onError(QBSignalingChannel.PacketType state, QBChatException e) {
            lo.g("error while establishing connection" + e.getLocalizedMessage());
        }

        @Override
        public void onCall(ConnectionConfig connectionConfig) {
            VideoSenderChannel senderChannel = activeChannels.get(participantId);
            senderChannel.addSignalingIgnoreFilter(this, new SignalingIgnoreFilter.Equals(
                    QBSignalingChannel.PacketType.qbvideochat_call));
            CallConfig callConfig = (CallConfig) connectionConfig;
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(
                    callConfig.getSessionDescription());
            lo.g("onCall " + callConfig.getCallStreamType().toString());
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
