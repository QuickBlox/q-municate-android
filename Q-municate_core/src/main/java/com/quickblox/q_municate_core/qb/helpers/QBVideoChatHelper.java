package com.quickblox.q_municate_core.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.helper.Lo;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.q_municate_core.core.communication.SessionDescriptionWrapper;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.call.WorkingSessionPull;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.videochat.webrtc.QBVideoChannel;
import com.quickblox.videochat.webrtc.model.CallConfig;
import com.quickblox.videochat.webrtc.listener.*;
import com.quickblox.videochat.webrtc.model.ConnectionConfig;
import com.quickblox.videochat.webrtc.signaling.QBSignalingChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class QBVideoChatHelper extends BaseHelper {

    private final static int ACTIVE_SESSIONS_DEFAULT_SIZE = 5;

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;
    private Map<Integer, QBVideoChannel> activeChannelMap = new HashMap<Integer, QBVideoChannel>(
            ACTIVE_SESSIONS_DEFAULT_SIZE);

    private WorkingSessionPull workingSessionPull = new WorkingSessionPull(ACTIVE_SESSIONS_DEFAULT_SIZE);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private VideoSignalingListener videoSignalingListener;

    public QBVideoChatHelper(Context context) {
        super(context);
    }

    public QBVideoChannel getSignalingChannel(int participantId) {
        return activeChannelMap.get(participantId);
    }

    public void init(QBChatService chatService) {
        Lo.g("init videochat");
        this.chatService = chatService;
        this.chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new SignalingManagerListener());
        videoSignalingListener = new VideoSignalingListener();
    }

    public void initActivityClass(Class<? extends Activity> activityClass) {
        this.activityClass = activityClass;
    }

    public void closeSignalingChannel(ConnectionConfig connectionConfig) {
        WorkingSessionPull.WorkingSession session = workingSessionPull.getSession(
                connectionConfig.getConnectionSession());
        Lo.g("closeSignalingChannel sessionId="+connectionConfig.getConnectionSession());
        if (session != null  && session.isActive()) {
            session.cancel();
            startClearSessionTask(connectionConfig);
        }
    }

    private void startClearSessionTask(ConnectionConfig connectionConfig) {
        ClearSessionTask clearSessionTask = new ClearSessionTask(connectionConfig.getConnectionSession(),
                connectionConfig.getToUser().getId());
        scheduler.schedule(clearSessionTask, ConstsCore.DEFAULT_CLEAR_SESSION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    public QBVideoChannel makeSignalingChannel(int participantId) {
        QBWebRTCSignaling signaling = QBChatService.getInstance().getVideoChatWebRTCSignalingManager().createSignaling(
                participantId, null);
        QBVideoChannel signalingChannel = new QBVideoChannel(signaling);
        signalingChannel.addSignalingListener(videoSignalingListener);
        activeChannelMap.put(participantId, signalingChannel);
        return signalingChannel;
    }

    private class SignalingManagerListener implements QBVideoChatSignalingManagerListener {

        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                QBVideoChannel signalingChannel = new QBVideoChannel((QBWebRTCSignaling) qbSignaling);
                signalingChannel.addSignalingListener(videoSignalingListener);
                activeChannelMap.put(((QBWebRTCSignaling)qbSignaling).getParticipant(), signalingChannel);
            }
        }
    }

    private boolean isExistSameSession(String sessionId){
        WorkingSessionPull.WorkingSession session = workingSessionPull.getSession(sessionId);
        return (session != null );
    }

    private class VideoSignalingListener extends QBVideoChatWebRTCSignalingListenerImpl {

        @Override
        public void onError(QBSignalingChannel.PacketType state, QBChatException e) {
            Lo.g("error while establishing connection" + e.getLocalizedMessage());
        }

        @Override
        public void onCall(ConnectionConfig connectionConfig) {
            String sessionId = connectionConfig.getConnectionSession();
            Lo.g("onCall sessionId="+sessionId);
            if ( isExistSameSession(sessionId) || workingSessionPull.existActive() || activityClass == null) {
                return;
            }

            workingSessionPull.addSession(new CallSession(sessionId), sessionId);
            CallConfig callConfig = (CallConfig) connectionConfig;
            SessionDescriptionWrapper sessionDescriptionWrapper = new SessionDescriptionWrapper(
                    callConfig.getSessionDescription());
            Intent intent = new Intent(context, activityClass);
            intent.putExtra(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, ConstsCore.CALL_DIRECTION_TYPE.INCOMING);
            intent.putExtra(com.quickblox.videochat.webrtc.Consts.PLATFORM_EXTENSION, callConfig.getDevicePlatform());
            intent.putExtra(com.quickblox.videochat.webrtc.Consts.ORIENTATION_EXTENSION, callConfig.getDeviceOrientation());
            intent.putExtra(ConstsCore.CALL_TYPE_EXTRA, callConfig.getCallStreamType());
            intent.putExtra(com.quickblox.videochat.webrtc.Consts.SESSION_ID_EXTENSION, sessionId);
            User friend = FriendUtils.createUser(callConfig.getFromUser());
            intent.putExtra(ConstsCore.EXTRA_FRIEND, friend);
            intent.putExtra(ConstsCore.REMOTE_DESCRIPTION, sessionDescriptionWrapper);
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