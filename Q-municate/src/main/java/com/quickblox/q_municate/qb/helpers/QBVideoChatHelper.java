package com.quickblox.q_municate.qb.helpers;

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
import com.quickblox.q_municate.core.communication.SessionDescriptionWrapper;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.qb.helpers.call.WorkingSessionPull;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.FriendUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QBVideoChatHelper extends BaseHelper {

    private final static int ACTIVE_SESSIONS_DEFAULT_SIZE = 5;
    private final Lo lo = new Lo(this);

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;
    private Map<Integer, VideoSenderChannel> activeChannelMap = new HashMap<Integer, VideoSenderChannel>(
            ACTIVE_SESSIONS_DEFAULT_SIZE);

    private WorkingSessionPull workingSessionPull = new WorkingSessionPull(ACTIVE_SESSIONS_DEFAULT_SIZE);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private VideoSignalingListener videoSignalingListener;

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
        videoSignalingListener = new VideoSignalingListener();
    }

    public void closeSignalingChannel(ConnectionConfig connectionConfig) {
        WorkingSessionPull.WorkingSession session = workingSessionPull.getSession(
                connectionConfig.getConnectionSession());
        lo.g("closeSignalingChannel sessionId="+connectionConfig.getConnectionSession());
        if (session != null  && session.isActive()) {
            session.cancel();
            startClearSessionTask(connectionConfig);
        }
    }

    private void startClearSessionTask(ConnectionConfig connectionConfig) {
        ClearSessionTask clearSessionTask = new ClearSessionTask(connectionConfig.getConnectionSession(),
                connectionConfig.getToUser().getId());
        scheduler.schedule(clearSessionTask, Consts.DEFAULT_CLEAR_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public VideoSenderChannel makeSignalingChannel(int participantId) {
        QBSignaling signaling = QBChatService.getInstance().getSignalingManager().createSignaling(
                participantId, null);
        VideoSenderChannel signalingChannel = new VideoSenderChannel(signaling);
        signalingChannel.addSignalingListener(videoSignalingListener);
        activeChannelMap.put(participantId, signalingChannel);
        return signalingChannel;
    }

    private class SignalingManagerListener implements QBSignalingManagerListener {

        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                VideoSenderChannel signalingChannel = new VideoSenderChannel(qbSignaling);
                signalingChannel.addSignalingListener(videoSignalingListener);
                activeChannelMap.put(qbSignaling.getParticipant(), signalingChannel);
            }
        }
    }

    private class VideoSignalingListener extends SignalingListenerImpl {

        @Override
        public void onError(QBSignalingChannel.PacketType state, QBChatException e) {
            lo.g("error while establishing connection" + e.getLocalizedMessage());
        }

        @Override
        public void onCall(ConnectionConfig connectionConfig) {
            String sessionId = connectionConfig.getConnectionSession();
            lo.g("onCall sessionId="+sessionId);
            WorkingSessionPull.WorkingSession session = workingSessionPull.getSession(sessionId);
            if ( (session != null && session.isActive()) || workingSessionPull.existActive() ) {
                return;
            }

            workingSessionPull.addSession(new CallSession(sessionId), sessionId);
            CallConfig callConfig = (CallConfig) connectionConfig;
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(
                    callConfig.getSessionDescription());
            Intent intent = new Intent(context, activityClass);
            intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRA, Consts.CALL_DIRECTION_TYPE.INCOMING);
            intent.putExtra(WebRTC.PLATFORM_EXTENSION, callConfig.getDevicePlatform());
            intent.putExtra(WebRTC.ORIENTATION_EXTENSION, callConfig.getDeviceOrientation());
            intent.putExtra(Consts.CALL_TYPE_EXTRA, callConfig.getCallStreamType());
            intent.putExtra(WebRTC.SESSION_ID_EXTENSION, sessionId);
            Friend friend = FriendUtils.createFriend(callConfig.getFromUser());
            intent.putExtra(Consts.EXTRA_FRIEND, friend);
            intent.putExtra(Consts.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        }
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
            WorkingSessionPull.WorkingSession workingSession = workingSessionPull.removeSession(sessionId);
        }
    }

    private class CallSession implements WorkingSessionPull.WorkingSession {

        private AtomicBoolean status;

        CallSession(String session) {
            status = new AtomicBoolean(true);
        }

        @Override
        public boolean isActive() {
            return status.get();
        }

        public void setStatus(boolean status) {
            this.status.set(status);
        }

        @Override
        public void cancel() {
            status.set(false);
        }
    }
}
