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
import com.quickblox.module.videochat_webrtc.utils.SignalingListenerImpl;
import com.quickblox.qmunicate.core.communication.SessionDescriptionWrapper;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.FriendUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class QBVideoChatHelper extends BaseHelper {

    private final static int ACTIVE_SESSIONS_DEFAULT_SIZE = 5;
    private final Lo lo = new Lo(this);

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;
    private Map<Integer, VideoSenderChannel> activeChannelMap = new HashMap<Integer, VideoSenderChannel>(
            ACTIVE_SESSIONS_DEFAULT_SIZE);
    private Map<String, Boolean> activeSessionMap = new HashMap<String, Boolean>(
            ACTIVE_SESSIONS_DEFAULT_SIZE);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    public QBVideoChatHelper(Context context) {
        super(context);
    }

    public VideoSenderChannel getSignalingChannel(int participantId) {
        return activeChannelMap.get(participantId);
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
                if (activeChannelMap.containsKey(qbSignaling.getParticipant())) {
                    return;
                }
                VideoSenderChannel signalingChannel = new VideoSenderChannel(qbSignaling);
                VideoSignalingListener videoSignalingListener = new VideoSignalingListener(
                        qbSignaling.getParticipant());
                signalingChannel.addSignalingListener(videoSignalingListener);
                activeChannelMap.put(qbSignaling.getParticipant(), signalingChannel);
            }
        }
    }

    public void closeSignalingChannel(ConnectionConfig connectionConfig) {
        activeSessionMap.put(connectionConfig.getConnectionSession(), false);
        ClearSessionTask clearSessionTask = new ClearSessionTask(connectionConfig.getConnectionSession(),
                connectionConfig.getToUser().getId());
        scheduler.schedule(clearSessionTask, Consts.DEFAULT_CLEAR_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public VideoSenderChannel makeSignalingChannel(int participantId) {
        QBSignaling signaling = QBChatService.getInstance().getSignalingManager().createSignaling(
                participantId, null);
        VideoSenderChannel signalingChannel = new VideoSenderChannel(signaling);
        activeChannelMap.put(participantId, signalingChannel);
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
            if (activeSessionMap.containsKey(
                    connectionConfig.getConnectionSession()) || isExistRunningSession()) {
                return;
            }
            activeSessionMap.put(connectionConfig.getConnectionSession(), true);
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

    private boolean isExistRunningSession() {
        for (Map.Entry<String, Boolean> entry : activeSessionMap.entrySet()) {
            if (entry.getValue()) {
                return true;
            }
        }
        return false;
    }

    private class ClearSessionTask extends TimerTask {

        private String sessionId;
        private int opponentId;

        ClearSessionTask(String sessionId, int opponentId) {
            this.sessionId = sessionId;
            this.opponentId = opponentId;
        }

        @Override
        public void run() {
            lo.g("clearing session running");
            if (!activeSessionMap.get(sessionId)) {
                activeSessionMap.remove(sessionId);
                VideoSenderChannel videoSenderChannel = activeChannelMap.remove(opponentId);
                if (videoSenderChannel != null) {
                    videoSenderChannel.close();
                }
            }
        }
    }
}
