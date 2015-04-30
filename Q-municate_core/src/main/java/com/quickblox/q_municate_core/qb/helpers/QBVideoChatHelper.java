package com.quickblox.q_municate_core.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.call.SessionManager;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperListener;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.SmackSignallingProcessorCallback;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoRendererGui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QBVideoChatHelper extends BaseHelper {

    private final static int ACTIVE_SESSIONS_DEFAULT_SIZE = 5;
    private static final String TAG = QBVideoChatHelper.class.getSimpleName();

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private List<VideoChatHelperListener> videoChatListenersList;

    // Videochat listeners
    private SessionCallbacksListener sessionCallbacksListener;
    private VideoTracksCallbacksListener videoTracksCallbacksListener;
    private SmackSignallingProcessorCallback smackMessageProcessorCallbacksListener;


    // VideoChat
    private GLSurfaceView renderingView;
    private String currentSession;


    // inner classes instances
    private SessionManager sessionManager;
    private List<SessionVideoTracksPull> sessionVideoTracksList;
    private VideoCallRendererCallbacks defaultRenderers;
    private QBRTCSessionDescription currentSessionDescription;


    public QBVideoChatHelper(Context context) {
        super(context);
        Log.d("CALL_INTEGRATION", "construct  QBVideoChatHelper");
        videoChatListenersList = new ArrayList<>();
        sessionVideoTracksList = new ArrayList<>();
        // init inner classes
        sessionManager = new SessionManager();
    }

    public void init(QBChatService chatService) {
        Log.d("CALL_INTEGRATION", "init QBVideoChatHelper");
        this.chatService = chatService;
        this.chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new SignallingManagerListenerImpl());
        sessionCallbacksListener = new SessionCallbacksListener();
        videoTracksCallbacksListener = new VideoTracksCallbacksListener();
        smackMessageProcessorCallbacksListener = new SmackSignallinProcessorgCallbackListener();

        Log.d("CALL_INTEGRATION"," Init call client");
        if(!QBRTCClient.isInitiated()) {
            QBRTCClient.init();
        }

        setUpCallClient();
    }

    private void setUpCallClient() {

        QBRTCClient.initTaskExecutor();

        Log.d("CALL_INTEGRATION","Add callbacks listeners");
        QBRTCClient.getInstance().addSmackMessagesProcessorCallbacksListener(getSmackSignallingProcessorCallback());
        QBRTCClient.getInstance().addSessionCallbacksListener(getSessionCallbacksListener());
        QBRTCClient.getInstance().addVideoTrackCallbacksListener(getVideoTracksCallbacksListener());
    }

    public void initActivityClass(Class<? extends Activity> activityClass) {
        Log.d("CALL_INTEGRATION", "init QBVideoChatHelper with activity");
        this.activityClass = activityClass;
    }


    // ---------------------- PUBLIC METHODS ----------------------------- //

    public  void  disposeAllResources(){
        currentSessionDescription = null;
        currentSession = null;
    }

    /**
     * @param listener
     */
    public void addVideoChatHelperListener(VideoChatHelperListener listener) {
        videoChatListenersList.add(listener);
    }


    public void removeVideoChatHelperListener(VideoChatHelperListener listener) {
        videoChatListenersList.remove(listener);
    }

    /**
     * Set view for video rendering
     *
     * @param videoView
     */
    public void addVideoView(GLSurfaceView videoView) {

        Log.d("CALL_INTEGRATION", "QBVideoChatHelper. addVideoView");

        renderingView = videoView;
        VideoRendererGui.ScalingType scaleType = VideoRendererGui.ScalingType.SCALE_ASPECT_FILL;

        defaultRenderers = new VideoCallRendererCallbacks();

        VideoRenderer.Callbacks remoteCallback = VideoRendererGui.create(0, 0, 100, 100, scaleType, true);
//        VideoRenderer remoteRenderer = new VideoRenderer(remoteCallbacks);
        defaultRenderers.setRemoteRendererCallback(remoteCallback);

        VideoRenderer.Callbacks localCallback = VideoRendererGui.create(70, 0, 30, 30, scaleType, true);
//        VideoRenderer localRenderer = new VideoRenderer(localCallbacks);
        defaultRenderers.setLocalRendererCallback(localCallback);
    }

    /**
     * Get session by id
     *
     * @param sessionID
     * @return
     */
    public QBRTCSession getSessionById(String sessionID) {
        return sessionManager.getSession(sessionID);
    }

    /**
     * Get session by id
     *
     * @return
     */
    public QBRTCSession getCurrentSession() {
        return sessionManager.getSession(currentSession);
    }

    private void setCurrentSession(QBRTCSession session) {
        currentSession = session.getSessionID();
    }

    public void setCurrentSessionId(String sesionId) {
        this.currentSession = sesionId;
    }


    public SessionCallbacksListener getSessionCallbacksListener() {
        return sessionCallbacksListener;
    }

    public VideoTracksCallbacksListener getVideoTracksCallbacksListener() {
        return videoTracksCallbacksListener;
    }

    public SmackSignallingProcessorCallback getSmackSignallingProcessorCallback() {
        return smackMessageProcessorCallbacksListener;
    }

    /**
     * Start call logic
     */
    public void startCall(Map<String, String> userInfo, List<Integer> opponents, QBRTCTypes.QBConferenceType qbConferenceType) {
        if(QBRTCClient.isClosed()) {
            setUpCallClient();
        }

        Log.d("CALL_INTEGRATION", "QBVideoChatHelper. Start call logic starts");
        QBRTCSession newSessionWithOpponents = QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, qbConferenceType);
        sessionManager.addSession(newSessionWithOpponents);
        setCurrentSession(newSessionWithOpponents);
        QBRTCSession session = sessionManager.getSession(currentSession);
        session.startCall(userInfo);
    }

    /**
     * Reject call logic
     */
    public void acceptCall(Map<String, String> userInfo) {
        if(QBRTCClient.isClosed()) {
            setUpCallClient();
        }
        if (sessionManager.getSession(currentSession) != null){
            Log.d("CALL_INTEGRATION", "QBVideoChatHelper. Accept call logic starts");
            sessionManager.getSession(currentSession).acceptCall(userInfo);
        }
    }


    /**
     * Reject call logic
     */
    public void rejectCall(Map<String, String> userInfo) {
//        if (sessionManager.getSession(currentSession) != null) {
//            Log.d("CALL_INTEGRATION", "QBVideoChatHelper. Reject call logic starts");
//            sessionManager.getSession(currentSession).rejectCall(userInfo);
//        }
    }

    /**
     * HangUp call logic
     */
    public void hangUpCall(Map<String, String> userInfo) {
        if (sessionManager.getSession(currentSession) != null) {
            Log.d("CALL_INTEGRATION", "QBVideoChatHelper. HangUp call logic starts");
            sessionManager.getSession(currentSession).hangUp(userInfo);
        }
    }


    /**
     * If state is true than mic will be enabled
     *
     * @param micState
     */
    public void setMicState(boolean micState) {
        sessionManager.getSession(currentSession).setAudioEnabled(micState);
    }

    /**
     * If state is true than cam will be enabled
     *
     * @param camState
     */
    public void setCamState(boolean camState) {
        sessionManager.getSession(currentSession).setVideoEnabled(camState);
    }

    /**
     * Switch betveen phone speaker and loudspeaker
     */
    public void switchMic() {
        sessionManager.getSession(currentSession).switchAudioOutput();
    }

    /**
     * If state is true than mic will be enabled
     */
    public void switchCam() {
        sessionManager.getSession(currentSession).switchCapturePosition();
    }


    private class VideoTracksCallbacksListener implements QBRTCClientVideoTracksCallbacks {

        @Override
        public void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack) {
            Log.d("CALL_INTEGRATION", "QBVideoChatHelper. onLocalVideoTrackReceive");

            for (VideoChatHelperListener listener : videoChatListenersList){
                listener.onLocalVideoTrackReceive(session, videoTrack);
            }

//            videoTrack.addRenderer(new VideoRenderer(defaultRenderers.getLocalRendererCallback()));
//
//            SessionVideoTracksPull pull = getSessionVideoTracksPullBySession(session);
//
//            if (pull == null) {
//                pull = new SessionVideoTracksPull(session);
//                sessionVideoTracksList.add(pull);
//            }
//
//            pull.setLocalVideoTrack(videoTrack);
        }

        @Override
        public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
            Log.d("CALL_INTEGRATION", "QBVideoChatHelper. onRemoteVideoTrackReceive");


            for (VideoChatHelperListener listener : videoChatListenersList){
                listener.onRemoteVideoTrackReceive(session, videoTrack, userID);
            }

//            videoTrack.addRenderer(new VideoRenderer(defaultRenderers.getRemoteRendererCallback()));
//
//            SessionVideoTracksPull pull = getSessionVideoTracksPullBySession(session);
//
//            if (pull == null) {
//                pull = new SessionVideoTracksPull(session);
//                sessionVideoTracksList.add(pull);
//            }
//
//            pull.addRemoteVideoTrack(userID, videoTrack);
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

    private class SmackSignallinProcessorgCallbackListener implements SmackSignallingProcessorCallback {

        @Override
        public void onReceiveCallFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription, SessionDescription sessionDescription) {
            if (activityClass != null) {
                Log.d("CALL_INTEGRATION", "SMACK SIGNALLING onReceiveCallFromUser");
                if (currentSessionDescription == null) {
                    if(QBRTCClient.isClosed()) {
                        setUpCallClient();
                    }
                    currentSessionDescription = qbrtcSessionDescription;
                    startCallActivity(qbrtcSessionDescription);
                } else {
                    Log.d("CALL_INTEGRATION", "currentSessionDescription isn't null");
                }
            }else {
                Log.d("CALL_INTEGRATION", "Activity class wasn't sett till now");
            }
        }



        @Override
        public void onReceiveAcceptFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription, SessionDescription sessionDescription) {
            Log.d("CALL_INTEGRATION", "SMACK SIGNALLING onReceiveAcceptFromUser");


        }

        @Override
        public void onReceiveRejectFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
            Log.d("CALL_INTEGRATION", "SMACK SIGNALLING onReceiveRejectFromUser");
            if (currentSessionDescription != null && currentSessionDescription.equals(qbrtcSessionDescription)) {
                currentSessionDescription = null;
            }
        }

        @Override
        public void onReceiveIceCandidatesFromUser(List<IceCandidate> iceCandidates, Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
            Log.d("CALL_INTEGRATION", "SMACK SIGNALLING onReceiveIceCandidatesFromUser");
        }

        @Override
        public void onReceiveUserHungUpCall(Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
            Log.d("CALL_INTEGRATION", "SMACK SIGNALLING onReceiveUserHungUpCall");
            if (currentSessionDescription != null && currentSessionDescription.equals(qbrtcSessionDescription)) {
                currentSessionDescription = null;
            }
        }

        @Override
        public void onAddUserNeed(Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
            Log.d("CALL_INTEGRATION", "SMACK SIGNALLING onAddUserNeed");
        }
    }


    private class SessionCallbacksListener implements QBRTCClientSessionCallbacks {


        //  ERROR
        //  Lo.g("error while establishing connection" + e.getLocalizedMessage());

        @Override
        public void onReceiveNewSession(QBRTCSession session) {

            Log.d("CALL_INTEGRATION", "START_CALL_ACTIVITY");

            if (currentSession == null) {
                Log.d(TAG, "onReceiveNewSession");

                setCurrentSession(session);
                sessionManager.addSession(session);

                for (VideoChatHelperListener listener : videoChatListenersList){
                    listener.onClientReady();
                }

//                startCallActivity(session.getSessionDescription());

//            } else if (sessionManager.getSession(currentSession).equals(session.getSessionID())) {
//                Log.d("CALL_INTEGRATION", "Session " + session.getSessionID()+ "wasn't accepted till now");
            }else{
                sessionManager.addSession(session);
                session.rejectCall(null);
            }
        }

        @Override
        public void onUserNotAnswer(QBRTCSession session, Integer integer) {
            Log.d("CALL_INTEGRATION", "onUserNotAnswer");
            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onUserNotAnswer(session, integer);
            }
        }

        @Override
        public void onCallRejectByUser(QBRTCSession session, Integer integer, Map<String, String> map) {
            Log.d("CALL_INTEGRATION", "onCallRejectByUser");
            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onCallRejectByUser(session, integer, map);
            }
        }

        @Override
        public void onReceiveHangUpFromUser(QBRTCSession session, Integer integer) {
            Log.d("CALL_INTEGRATION", "onReceiveHangUpFromUser");
            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onReceiveHangUpFromUser(session, integer);
            }
        }

        @Override
        public void onSessionClosed(QBRTCSession session) {
            Log.d("CALL_INTEGRATION", "onSessionClosed");
            if (session.getSessionID().equals(currentSession)) {
                for (VideoChatHelperListener listener : videoChatListenersList) {
                    listener.onSessionClosed(session);
                }
                if (session.getSessionID().equals(currentSession)) {
                    Log.d("CALL_INTEGRATION", "Stop session");
                    currentSession = null;

                    if (currentSessionDescription != null){
                        currentSessionDescription = null;
                    }
                }
            }
        }

        @Override
        public void onSessionStartClose(QBRTCSession session) {
            Log.d("CALL_INTEGRATION", "onSessionStartClose");
            for (VideoChatHelperListener listener : videoChatListenersList) {
                listener.onSessionStartClose(session);
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
        context.getApplicationContext().startActivity(intent);
    }


    private class SignallingManagerListenerImpl implements QBVideoChatSignalingManagerListener {
        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                Log.d("CALL_INTEGRATION","Create QBSignaling signalling");
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
}