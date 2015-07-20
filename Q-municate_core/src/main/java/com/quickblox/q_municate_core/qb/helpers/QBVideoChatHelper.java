package com.quickblox.q_municate_core.qb.helpers;


/**
 * The main class of video and audio calls.
 * All non UI logic and interaction with Quickblox SDK execute here.
 *
 *
 *
 * There are 4 inner classes:
 *
 * Two of them response for {@link com.quickblox.videochat.webrtc.QBRTCClient} callbacks processing, it is:
 * SessionCallbacksListener - session callbacks (receive new call, call accepted, call rejected, call hanged up)
 * VideoTracksCallbacksListener - new local {@link org.webrtc.VideoTrack} or remote ones was created and requires to set them up {@link com.quickblox.videochat.webrtc.view.VideoCallBacks}
 *
 * {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper.SignallingManagerListenerImpl} - listen creation of new {@link com.quickblox.chat.QBWebRTCSignaling}
 * and once it was created set it up to QBRTCClient via {@link com.quickblox.videochat.webrtc.QBRTCClient#addSignaling(com.quickblox.chat.QBWebRTCSignaling)}
 *
 * {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper.RTCSignallingMessageProcessorCallbackListener} added to process calls in background until {@link android.content.Context}
 * instance was created.
 *
 *
 *
 *                          <h1> ----- PROCESSING CALLS IN BACKGROUND ----- </h1>
 *
 * For background calls processing you should wake next steps.
 *
 * <ul>
 *     <li> Init {@link com.quickblox.chat.listeners.QBVideoChatSignalingListener} listener</li>
 *     <li> Set up {@link com.quickblox.videochat.webrtc.QBRTCClient} and add listeners to it;</li>
 *     <li> Call {@link com.quickblox.videochat.webrtc.QBRTCClient.getInstance().prepareToProcessCalls(this);}</li>
 *     <li> Start UI components when income call was received and subscribe on callbacks from {@link com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperSessionListener};</li>
 *     <li> Throw call to UI components via {@link com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperSessionListener};</li>
 *</ul>
 *
 *                          <h2> --- ADDITIONAL DESCRIPTION for list about --- </h2>
 *
 * I. Process of initiation {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper.SignallingManagerListenerImpl}
 * listener is shown in {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper.SignallingManagerListenerImpl} inner class
 *
 *
 * II. Process of sating up {@link com.quickblox.videochat.webrtc.QBRTCClient} are represented below:
 *  -- ex: Setting up QBRTCClient --
 * <code>
 *  QBRTCClient.getInstance().addRTCSignallingMessageProcessorCallbackListener(getRTCSignallingProcessorCallback());
 *  QBRTCClient.getInstance().addSessionCallbacksListener(getSessionCallbacksListener());
 *  QBRTCClient.getInstance().addVideoTrackCallbacksListener(getVideoTracksCallbacksListener());
 * </code>
 * NOTE!!! If you don't set up QBRTClient callbacks wont be notified about current calls' state
 *
 * III. Call {@link com.quickblox.videochat.webrtc.QBRTCClient#prepareToProcessCalls(android.content.Context)} uses
 * to notify {@link com.quickblox.videochat.webrtc.QBRTCClient} that you are ready listening calls.
 * PAY ATTENTION: When you recreate you context instance you should renew link on it in {@link com.quickblox.videochat.webrtc.QBRTCClient}.
 *
 * IV. Start UI components when income call was received and subscribe on callbacks from the VideoChatHelper mean:
 * <ul>
 *     <li>Start your call activity</li>
 *     <li>Subscribe call activity on listening callbacks from VideoChatHelper</li>
 * </ul>
 *
 *
 */

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
import com.quickblox.q_municate_core.qb.helpers.call.SessionManager;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatHelperSessionListener;
import com.quickblox.q_municate_core.qb.helpers.call.VideoChatVideoTracksListener;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.QBRTCTypes;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import org.webrtc.VideoCapturerAndroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class QBVideoChatHelper extends BaseHelper {

    private static final String TAG = QBVideoChatHelper.class.getSimpleName();
    private static final String CALL_INTEGRATION = "CALL_INTEGRATION";

    private QBChatService chatService;
    private Class<? extends Activity> activityClass;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3); //TODO ненужное поле ?
    private List<VideoChatHelperSessionListener> videoChatHelperSessionListeners;
    private List<VideoChatVideoTracksListener> videoChatVideoTracksListeners;

    // Videochat listeners
    private SessionCallbacksListener sessionCallbacksListener;
    private VideoTracksCallbacksListener videoTracksCallbacksListener;

    // inner classes instances
    private SessionManager sessionManager;
    private boolean isClientClosed;
    private VideoHelperStates vieoChatHelperState;
    private Set<String> incomingSessionsSet; //TODO ненужное поле ?


    public QBVideoChatHelper(Context context) {
        super(context);
        Log.d(CALL_INTEGRATION, "construct  QBVideoChatHelper");
        videoChatHelperSessionListeners = new ArrayList<>();
        videoChatVideoTracksListeners = new ArrayList<>();

        // init inner classes
        sessionManager = new SessionManager();
    }

    public void init(QBChatService chatService) {
        Log.d(CALL_INTEGRATION, "init QBVideoChatHelper");
        this.chatService = chatService;
        this.chatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new SignallingManagerListenerImpl());
        incomingSessionsSet = new HashSet<>();

        sessionCallbacksListener = new SessionCallbacksListener();
        videoTracksCallbacksListener = new VideoTracksCallbacksListener();

        vieoChatHelperState = VideoHelperStates.WAIT_FOR_CALL;

        Log.d(CALL_INTEGRATION, " Init call client");
        setUpCallClient();
    }

    private void setUpCallClient() {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. setUpCallClient");
        isClientClosed = false;

        QBRTCConfig.setAnswerTimeInterval(45);

        QBRTCClient.getInstance().setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(String s) {
                Log.e(CALL_INTEGRATION, "Error on cams");

                for (VideoChatHelperSessionListener listener : videoChatHelperSessionListeners) {
                    listener.onError(s);
                }
            }
        });

        Log.d(CALL_INTEGRATION, "Add callbacks listeners");
        QBRTCClient.getInstance().addSessionCallbacksListener(getSessionCallbacksListener());
        QBRTCClient.getInstance().addVideoTrackCallbacksListener(getVideoTracksCallbacksListener());

        Log.d(CALL_INTEGRATION, "CallActivity. QBRTCClient start listening calls");
        QBRTCClient.getInstance().prepareToProcessCalls(context);
    }

    public void initActivityClass(Class<? extends Activity> activityClass) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. init QBVideoChatHelper with activity");
        this.activityClass = activityClass;
    }

    // ---------------------- PUBLIC METHODS ----------------------------- //

    public void setClientClosed() {
        this.isClientClosed = true;
    }

    public void addVideoChatHelperSessionListener(VideoChatHelperSessionListener listener) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. addVideoChatHelperListener");
        videoChatHelperSessionListeners.add(listener);
    }

    public void removeVideoChatHelperSessionListener(VideoChatHelperSessionListener listener) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. removeVideoChatHelperSessionListener");
        videoChatHelperSessionListeners.remove(listener);
    }

    public void addVideoChatVideoTracksListener(VideoChatVideoTracksListener listener) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. addVideoChatVideoTracksListener");
        videoChatVideoTracksListeners.add(listener);
    }

    public void removeVideoChatHelperVideoTracksListener(VideoChatVideoTracksListener listener) {
        Log.d(CALL_INTEGRATION, "QBVideoChatHelper. removeVideoChatHelperListener");
        videoChatVideoTracksListeners.remove(listener);
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

            if(getCurrentSession() != null)
                Log.d(CALL_INTEGRATION, "QBVideoChatHelper. startCall cur session is " + getCurrentSession().getSessionID());

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

            for (VideoChatVideoTracksListener listener : videoChatVideoTracksListeners) {
                listener.onLocalVideoTrackReceive(videoTrack);
            }
        }

        @Override
        public void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onRemoteVideoTrackReceive");

            for (VideoChatVideoTracksListener listener : videoChatVideoTracksListeners) {
                listener.onRemoteVideoTrackReceive(videoTrack, userID);
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
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. RTCClient. onReceiveNewSession for session id " + session.getSessionID());
            if(getCurrentSession() != null)
                Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onSeonReceiveNewSessionssionClosed cur session is " + getCurrentSession());

            boolean isEqualsLastSessionId = session.getSessionID().equals(sessionManager.getLastSessionId());

            if (getVideoChatHelperState().ordinal() < VideoHelperStates.RTC_CLIENT_PROCESS_CALLS.ordinal()) {
                // Check is new income session was early
                if (!isEqualsLastSessionId) {
                    setVideoChatHelperState(VideoHelperStates.RTC_CLIENT_PROCESS_CALLS);
                    Log.d(CALL_INTEGRATION, "On client receive new session");
                    startCallActivity(session.getSessionDescription());
                    setCurrentSession(session);

                } else {
                    Log.d(CALL_INTEGRATION, "Call with same ID received");
                }
            } else {
                if (!isEqualsLastSessionId) {
                    sessionManager.addSession(session);
                    session.rejectCall(session.getUserInfo());
                }
            }
        }

        @Override
        public void onUserNotAnswer(QBRTCSession session, Integer integer) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onUserNotAnswer");
            for (VideoChatHelperSessionListener listener : videoChatHelperSessionListeners) {
                listener.onUserNotAnswer(integer);
            }
        }

        @Override
        public void onCallRejectByUser(QBRTCSession session, Integer integer, Map<String, String> map) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onCallRejectByUser for session id " + session.getSessionID());
            if(getCurrentSession() != null)
                Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onCallRejectByUser cur session is " + getCurrentSession().getSessionID());
            if (session.equals(getCurrentSession())) {
                for (VideoChatHelperSessionListener listener : videoChatHelperSessionListeners) {
                    listener.onCallRejectByUser(integer, map);
                }
            }
        }

        @Override
        public void onReceiveHangUpFromUser(QBRTCSession session, Integer integer) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onReceiveHangUpFromUser for session id " + session.getSessionID());
            if(getCurrentSession() != null)
                Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onReceiveHangUpFromUser cur session is " + getCurrentSession().getSessionID());

            if (session.equals(getCurrentSession())) {
                for (VideoChatHelperSessionListener listener : videoChatHelperSessionListeners) {
                    listener.onReceiveHangUpFromUser(integer);
                }
            }
        }

        @Override
        public void onSessionClosed(QBRTCSession session) {
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onSessionClosed for session id " + session.getSessionID());
            if(getCurrentSession() != null)
            Log.d(CALL_INTEGRATION, "QBVideoChatHelper. onSessionClosed cur session is " + getCurrentSession().getSessionID());


            if (session.equals(getCurrentSession())) {
                Log.d(CALL_INTEGRATION, "Notify listebers in count of " +
                        videoChatHelperSessionListeners.size() + " about onSessionClosed call");

                for (VideoChatHelperSessionListener listener : videoChatHelperSessionListeners) {
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
            for (VideoChatHelperSessionListener listener : videoChatHelperSessionListeners) {
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

    public enum VideoHelperStates {

        /**
         * There is no call signalling was receives. Instance wait for incoming signal.
         */
        WAIT_FOR_CALL,


        /**
         * Signalling are processing by {@link QBRTCClient}
         */
        RTC_CLIENT_PROCESS_CALLS;
    }
}