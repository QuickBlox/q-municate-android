package com.quickblox.q_municate_core.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.q_municate_core.core.exceptions.QBRTCSessionIsAbsentException;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.call.RTCSignallingMessageProcessorCallbackImpl;
import com.quickblox.q_municate_core.qb.helpers.call.SessionManager;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperListener;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.callbacks.RTCSignallingMessageProcessorCallback;
import com.quickblox.videochat.webrtc.view.QBGLVideoView;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.SessionDescription;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QBVideoChatHelper extends BaseHelper {

    private static final String TAG = QBVideoChatHelper.class.getSimpleName();
    private static final String CALL_INTEGRATION = "CALL_INTEGRATION";

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private List<VideoChatHelperListener> videoChatListenersList;

    // Videochat listeners
    private SessionCallbacksListener sessionCallbacksListener;
    private VideoTracksCallbacksListener videoTracksCallbacksListener;
    private RTCSignallingMessageProcessorCallback smackMessageProcessorCallbacksListener;

    // VideoChat
    private QBGLVideoView renderingView;
    private QBRTCSession currentSession;

    // inner classes instances
    private SessionManager sessionManager;
    private List<SessionVideoTracksPull> sessionVideoTracksList;
    private boolean isClientClosed;
    private String currentSessionId;
    private VideoHelperStates vieoChatHelperState;


    public QBVideoChatHelper(Context context) {
        super(context);
        Log.d(CALL_INTEGRATION, "construct  QBVideoChatHelper");
        videoChatListenersList = new ArrayList<>();
        sessionVideoTracksList = new ArrayList<>();
        // init inner classes
        sessionManager = new SessionManager();
    }

    public void init(QBChatService chatService) {
        Log.d(CALL_INTEGRATION, "init QBVideoChatHelper");
        this.chatService = chatService;
        this.chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new SignallingManagerListenerImpl());
        sessionCallbacksListener = new SessionCallbacksListener();
        videoTracksCallbacksListener = new VideoTracksCallbacksListener();
        smackMessageProcessorCallbacksListener = new RTCSignallingMessageProcessorCallbackListener();

        vieoChatHelperState = VideoHelperStates.WAIT_FOR_CALL;

        Log.d(CALL_INTEGRATION, " Init call client");
        setUpCallClient();
    }

    private void setUpCallClient() {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. setUpCallClient");
        isClientClosed = false;

//        QBRTCConfig.setAnswerTimeInterval(60);

        QBRTCClient.getInstance().setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(String s) {
                Log.e(CALL_INTEGRATION, "Error on cams");

                for (VideoChatHelperListener listener : videoChatListenersList) {
                    listener.onError(s);
                }
            }
        });

        Log.d(CALL_INTEGRATION, "Add callbacks listeners");
        QBRTCClient.getInstance().addRTCSignallingMessageProcessorCallbackListener(getSmackSignallingProcessorCallback());
        QBRTCClient.getInstance().addSessionCallbacksListener(getSessionCallbacksListener());
        QBRTCClient.getInstance().addVideoTrackCallbacksListener(getVideoTracksCallbacksListener());
    }

    public void initActivityClass(Class<? extends Activity> activityClass) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. init QBVideoChatHelper with activity");
        this.activityClass = activityClass;
    }


    // ---------------------- PUBLIC METHODS ----------------------------- //

    public void setClientClosed() {
        this.isClientClosed = true;
    }

    /**
     * @param listener
     */
    public void addVideoChatHelperListener(VideoChatHelperListener listener) {
        Log.d("CALL_INTEGRATION", "QBVideoChatHelper. addVideoChatHelperListener");
        videoChatListenersList.add(listener);
    }

    public void removeVideoChatHelperListener(VideoChatHelperListener listener) {
        Log.d("CALL_INTEGRATION", "QBVideoChatHelper. removeVideoChatHelperListener");
        videoChatListenersList.remove(listener);
    }

    public QBRTCSession getCurrentSession() {
        return sessionManager.getCurrentSession();
    }

    private void setCurrentSession(QBRTCSession session) {
        sessionManager.addSession(session);
        sessionManager.setCurrentSession(session);
    }

    public VideoHelperStates getVideoChatHelperState() {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Current state is " + vieoChatHelperState.name());
        return vieoChatHelperState;
    }

    public void setVideoChatHelperState(VideoHelperStates state) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Set state to " + state.name());
        this.vieoChatHelperState = state;
    }

    public SessionCallbacksListener getSessionCallbacksListener() {
        return sessionCallbacksListener;
    }

    public VideoTracksCallbacksListener getVideoTracksCallbacksListener() {
        return videoTracksCallbacksListener;
    }

    public RTCSignallingMessageProcessorCallback getSmackSignallingProcessorCallback() {
        return smackMessageProcessorCallbacksListener;
    }

    /**
     * Start call logic
     */
    public void startCall(Map<String, String> userInfo, List<Integer> opponents, QBRTCTypes.QBConferenceType qbConferenceType) {
        if (getVideoChatHelperState() == VideoHelperStates.WAIT_FOR_CALL) {
            setVideoChatHelperState(VideoHelperStates.RTC_CLIENT_PROCESS_CALLS);

            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Start call logic starts");
            if (isClientClosed) {
                Log.d(CALL_INTEGRATION, "Reinit RTCClient");
                setUpCallClient();
            }

            QBRTCSession newSessionWithOpponents = QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, qbConferenceType);

            setCurrentSession(newSessionWithOpponents);
            newSessionWithOpponents.startCall(userInfo);
        }
    }

    /**
     * Accept call logic
     */
    public void acceptCall(Map<String, String> userInfo) throws QBRTCSessionIsAbsentException {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Accept call logic starts");
        if (isClientClosed) {
            Log.d(CALL_INTEGRATION, "Reinit RTCClient");
            setUpCallClient();
        }

        if (getCurrentSession() != null) {
            getCurrentSession().acceptCall(userInfo);
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }

    /**
     * Reject call logic
     */
    public void rejectCall(Map<String, String> userInfo) throws QBRTCSessionIsAbsentException {
        if (getCurrentSession() != null) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Reject call logic starts");
            getCurrentSession().rejectCall(userInfo);
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }

    /**
     * HangUp call logic
     */
    public void hangUpCall(Map<String, String> userInfo) throws QBRTCSessionIsAbsentException {
        if (getCurrentSession() != null) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. HangUp call logic starts");
            getCurrentSession().hangUp(userInfo);
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }

    /**
     * If state is true than mic will be enabled
     *
     * @param micState
     */
    public void setMicState(boolean micState) throws QBRTCSessionIsAbsentException {
        if (getCurrentSession() != null) {
            getCurrentSession().setAudioEnabled(micState);
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }

    /**
     * If state is true than cam will be enabled
     *
     * @param camState
     */
    public void setCamState(boolean camState) throws QBRTCSessionIsAbsentException {
        if (getCurrentSession() != null) {
            getCurrentSession().setVideoEnabled(camState);
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }

    /**
     * Switch between phone speaker and loudspeaker
     */
    public boolean switchMic() throws QBRTCSessionIsAbsentException {

        if (getCurrentSession() != null) {
            return getCurrentSession().switchAudioOutput();
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }

    /**
     * If state is true than mic will be enabled
     */
    public void switchCam(Runnable runnable) throws QBRTCSessionIsAbsentException {
        if (getCurrentSession() != null) {
            if (runnable == null) {
                new Runnable() {
                    @Override
                    public void run() {
                    }
                };
            }
            getCurrentSession().switchCapturePosition(runnable);
        } else {
            throw new QBRTCSessionIsAbsentException();
        }
    }


    /**
     * Class for VideoTracks processing
     */
    private class VideoTracksCallbacksListener implements QBRTCClientVideoTracksCallbacks {

        @Override
        public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onLocalVideoTrackReceive");

            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onLocalVideoTrackReceive(videoTrack);
            }
        }

        @Override
        public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onRemoteVideoTrackReceive");


            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onRemoteVideoTrackReceive(videoTrack, userID);
            }
        }
    }

    private SessionVideoTracksPull getSessionVideoTracksPullBySession(QBRTCSession session) {
        SessionVideoTracksPull result = null;
        for (SessionVideoTracksPull pull : sessionVideoTracksList) {
            if (pull.getSession().equals(session)) {
                result = pull;
            }
        }
        return result;
    }


    /**
     * Class for SmackSignalling processing
     */
    private class RTCSignallingMessageProcessorCallbackListener extends RTCSignallingMessageProcessorCallbackImpl {

        @Override
        public void onReceiveCallFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription, SessionDescription sessionDescription) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. SmackSignallingProcessorCallbackListener. onReceiveCallFromUser");
            if (activityClass != null) {
                if (getVideoChatHelperState().ordinal() < VideoHelperStates.RECEIVE_INCOME_CALL_MESSAGE.ordinal()) {
                    // Check is new income call was early
                    if (!qbrtcSessionDescription.getSessionId().equals(sessionManager.getLastSessionId())) {
                        Log.d(CALL_INTEGRATION, "Receive call from user");
                        setVideoChatHelperState(VideoHelperStates.RECEIVE_INCOME_CALL_MESSAGE);
                        startCallActivity(qbrtcSessionDescription);
                    } else {
                        Log.d(CALL_INTEGRATION, "Call with same ID received");
                    }
                } else {
                    Log.d(CALL_INTEGRATION, "Video chat helper already process some call");
                }
            } else {
                Log.d(CALL_INTEGRATION, "Activity class wasn't sett till now");
            }
        }


        @Override
        public void onReceiveUserHungUpCall(Integer opponentID, QBRTCSessionDescription qbrtcSessionDescription) {
            super.onReceiveUserHungUpCall(opponentID, qbrtcSessionDescription);
            if (!qbrtcSessionDescription.getSessionId().equals(sessionManager.getLastSessionId())) {
                for (VideoChatHelperListener listener : videoChatListenersList) {
                    listener.onReceiveHangUpFromUser(opponentID);
                }

                setVideoChatHelperState(VideoHelperStates.WAIT_FOR_CALL);
            }
        }
    }

    /**
     * Class for Signalling creation listening
     * Receive callbacks from RTCClient which manages sessions
     */
    private class SessionCallbacksListener implements QBRTCClientSessionCallbacks {

        @Override
        public void onReceiveNewSession(QBRTCSession session) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. RTCClient. onReceiveNewSession");
            boolean isEqualsLastSessionId = session.getSessionID().equals(sessionManager.getLastSessionId());

            if (getVideoChatHelperState().ordinal() < VideoHelperStates.RTC_CLIENT_PROCESS_CALLS.ordinal()) {
                // Check is new income session was early
                if (!isEqualsLastSessionId) {
                    setVideoChatHelperState(VideoHelperStates.RTC_CLIENT_PROCESS_CALLS);
                    Log.d(CALL_INTEGRATION, "On client receive new session");

                    setCurrentSession(session);

                    for (VideoChatHelperListener listener : videoChatListenersList) {
                        listener.onClientReady();
                    }
                } else {
                    Log.d(CALL_INTEGRATION, "Call with same ID received");
                }
            } else {
                if (!isEqualsLastSessionId) {
                    sessionManager.addSession(session);
                    session.rejectCall(getCurrentSession().getUserInfo());
                }
            }
        }

        @Override
        public void onUserNotAnswer(QBRTCSession session, Integer integer) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onUserNotAnswer");
            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onUserNotAnswer(integer);
            }
        }

        @Override
        public void onCallRejectByUser(QBRTCSession session, Integer integer, Map<String, String> map) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onCallRejectByUser");
            if (session.equals(getCurrentSession())) {
                for (VideoChatHelperListener listener : videoChatListenersList) {
                    listener.onCallRejectByUser(integer, map);
                }
            }
        }

        @Override
        public void onReceiveHangUpFromUser(QBRTCSession session, Integer integer) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onReceiveHangUpFromUser");
            if (session.equals(getCurrentSession())) {
                for (VideoChatHelperListener listener : videoChatListenersList) {
                    listener.onReceiveHangUpFromUser(integer);
                }
            }
        }

        @Override
        public void onSessionClosed(QBRTCSession session) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onSessionClosed");
            if (session.equals(getCurrentSession())) {
                Log.d(CALL_INTEGRATION, "Notify listebers in count of " +
                        videoChatListenersList.size() + " about onSessionClosed call");

                for (VideoChatHelperListener listener : videoChatListenersList) {
                    listener.onSessionClosed();
                }

                Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Stop session");
                setVideoChatHelperState(VideoHelperStates.WAIT_FOR_CALL);

                sessionManager.removeCurrentSession();
            }
        }

        @Override
        public void onSessionStartClose(QBRTCSession session) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onSessionStartClose");
            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onSessionStartClose();
            }
        }
    }

    private void startCallActivity(QBRTCSessionDescription sessionDescription) {
        User friend = new User(new QBUser(sessionDescription.getCallerID()));
        Intent intent = new Intent(context, activityClass);
        intent.putExtra(ConstsCore.CALL_DIRECTION_TYPE_EXTRA, ConstsCore.CALL_DIRECTION_TYPE.INCOMING);
        intent.putExtra(ConstsCore.CALL_TYPE_EXTRA, sessionDescription.getConferenceType());
        intent.putExtra(ConstsCore.EXTRA_FRIEND, friend);
        intent.putExtra(ConstsCore.USER_INFO, (HashMap) sessionDescription.getUserInfo());
        intent.putExtra(ConstsCore.SESSION_ID, sessionDescription.getSessionId());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. Start call activity. CALL_DIRECTION_TYPE_EXTRA " + ConstsCore.CALL_DIRECTION_TYPE.INCOMING +
                " CALL_TYPE_EXTRA " + sessionDescription.getConferenceType() + " EXTRA_FRIEND " + friend +
                " USER_INFO " + sessionDescription.getUserInfo() + "SESSION_ID" + sessionDescription.getSessionId());

        context.getApplicationContext().startActivity(intent);
    }


    private class SignallingManagerListenerImpl implements QBVideoChatSignalingManagerListener {
        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                Log.d(CALL_INTEGRATION, "QBVideoChatHelper. New QBSignaling was created " + qbSignaling);
                if (isClientClosed) {
                    Log.d(CALL_INTEGRATION, "Reinit RTCClient");
                    setUpCallClient();
                }

                QBRTCClient.getInstance().addSignaling((QBWebRTCSignaling) qbSignaling);

            }
        }
    }

    private class SessionVideoTracksPull {

        private QBRTCSession session;
        private QBRTCVideoTrack locacalVideoTrack;
        private Map<Integer, QBRTCVideoTrack> remoteVideoTrackMap;

        public SessionVideoTracksPull(QBRTCSession session) {
            this.session = session;
            remoteVideoTrackMap = new HashMap<>();
        }

        public QBRTCVideoTrack getLocacalVideoTrack() {
            return locacalVideoTrack;
        }

        public void setLocalVideoTrack(QBRTCVideoTrack locacalVideoTrack) {
            this.locacalVideoTrack = locacalVideoTrack;
        }

        public QBRTCVideoTrack getRemoteVideoTrack(Integer userId) {
            return remoteVideoTrackMap.get(userId);
        }

        public void addRemoteVideoTrack(Integer userID, QBRTCVideoTrack remoteVideoTrack) {
            remoteVideoTrackMap.put(userID, remoteVideoTrack);
        }

        public QBRTCSession getSession() {
            return session;
        }
    }


    private class VideoCallRendererCallbacks {

        private VideoRenderer.Callbacks localRenderer;
        private VideoRenderer.Callbacks remoteRenderer;

        public void setLocalRendererCallback(VideoRenderer.Callbacks renderer) {
            this.localRenderer = renderer;
        }

        public void setRemoteRendererCallback(VideoRenderer.Callbacks renderer) {
            this.remoteRenderer = renderer;
        }

        public VideoRenderer.Callbacks getLocalRendererCallback() {
            return localRenderer;
        }

        public VideoRenderer.Callbacks getRemoteRendererCallback() {
            return remoteRenderer;
        }
    }

    public enum VideoHelperStates {
        WAIT_FOR_CALL,
        RECEIVE_INCOME_CALL_MESSAGE,
        RTC_CLIENT_PROCESS_CALLS,
    }
}