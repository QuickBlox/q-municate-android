package com.quickblox.q_municate_core.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.q_municate_core.models.StartConversationReason;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;

import org.webrtc.CameraVideoCapturer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QBCallChatHelper extends BaseHelper {

    private static final String TAG = QBCallChatHelper.class.getSimpleName();

    private static final int MAX_OPPONENTS_COUNT = 1;
    private static final int DISCONNECT_TIME = 30;
    private static final int ANSWER_TIME_INTERVAL = 60;

    private QBRTCClient qbRtcClient;
    private Class<? extends Activity> activityClass;

    private QBRTCSession currentQbRtcSession;
    private QBRTCClientSessionCallbacks qbRtcClientSessionCallbacks;

    public QBCallChatHelper(Context context) {
        super(context);
    }

    public void init(QBChatService qbChatService) {
        Log.d(TAG, "init()");

        qbRtcClient = QBRTCClient.getInstance(context);

        qbChatService.getVideoChatWebRTCSignalingManager()
                .addSignalingManagerListener(new QBVideoChatSignalingManagerListenerImpl());

        qbRtcClient.addSessionCallbacksListener(new QBRTCClientSessionCallbacksImpl());

        setUpCallClient();
    }

    public void initActivityClass(Class<? extends Activity> activityClass) {
        Log.d(TAG, "initActivityClass()");
        this.activityClass = activityClass;
        Log.d("test_crash_1", "initActivityClass(), activityClass = " + activityClass);
    }

    public QBRTCSession getCurrentRtcSession() {
        return currentQbRtcSession;
    }

    public void initCurrentSession(QBRTCSession qbRtcSession, QBRTCSignalingCallback qbRtcSignalingCallback,
                                   QBRTCSessionConnectionCallbacks qbRtcSessionConnectionCallbacks) {
        this.currentQbRtcSession = qbRtcSession;
        initCurrentSession(qbRtcSignalingCallback, qbRtcSessionConnectionCallbacks);
    }

    public void initCurrentSession(QBRTCSignalingCallback qbRtcSignalingCallback,
                                   QBRTCSessionConnectionCallbacks qbRtcSessionConnectionCallbacks) {
        this.currentQbRtcSession.addSignalingCallback(qbRtcSignalingCallback);
        this.currentQbRtcSession.addSessionCallbacksListener(qbRtcSessionConnectionCallbacks);
    }

    public void releaseCurrentSession(QBRTCSignalingCallback qbRtcSignalingCallback,
                                      QBRTCSessionConnectionCallbacks qbRtcSessionConnectionCallbacks) {
        if (currentQbRtcSession != null) {
            currentQbRtcSession.removeSignalingCallback(qbRtcSignalingCallback);
            currentQbRtcSession.removeSessionCallbacksListener(qbRtcSessionConnectionCallbacks);
            currentQbRtcSession = null;
        }
    }

    private void setUpCallClient() {
        Log.d(TAG, "setUpCallClient()");

        QBRTCConfig.setMaxOpponentsCount(MAX_OPPONENTS_COUNT);
        QBRTCConfig.setDisconnectTime(DISCONNECT_TIME);
        QBRTCConfig.setAnswerTimeInterval(ANSWER_TIME_INTERVAL);
        QBRTCConfig.setDebugEnabled(true);

        qbRtcClient.prepareToProcessCalls();
    }

    private void startCallActivity(QBRTCSession qbRtcSession) {
        QMUser user = QMUserService.getInstance().getUserCache().get((long)qbRtcSession.getSessionDescription().getCallerID());

        if (user != null) {
            Log.d(TAG, "startCallActivity(), user = " + user);
            Log.d(TAG, "startCallActivity(), qbRtcSession.getConferenceType() = " + qbRtcSession
                    .getConferenceType());
            Log.d(TAG, "startCallActivity(), qbRtcSession.getSessionDescription() = " + qbRtcSession
                    .getSessionDescription());

            List<QBUser> qbUsersList = new ArrayList<>(1);
            qbUsersList.add(UserFriendUtils.createQbUser(user));
            Intent intent = new Intent(context, activityClass);
            intent.putExtra(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) qbUsersList);
            intent.putExtra(QBServiceConsts.EXTRA_START_CONVERSATION_REASON_TYPE,
                    StartConversationReason.INCOME_CALL_FOR_ACCEPTION);
            intent.putExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbRtcSession.getConferenceType());
            intent.putExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, qbRtcSession.getSessionDescription());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.getApplicationContext().startActivity(intent);
        } else {
            throw new NullPointerException("user is null!");
        }
    }

    public void addRTCSessionUserCallback(QBRTCClientSessionCallbacks qbRtcClientSessionCallbacks) {
        this.qbRtcClientSessionCallbacks = qbRtcClientSessionCallbacks;
    }

    public void removeRTCSessionUserCallback() {
        this.qbRtcClientSessionCallbacks = null;
    }

    private class QBVideoChatSignalingManagerListenerImpl implements QBVideoChatSignalingManagerListener {

        private final String TAG = QBVideoChatSignalingManagerListenerImpl.class.getSimpleName();

        @Override
        public void signalingCreated(QBSignaling qbSignaling, boolean createdLocally) {
            if (!createdLocally) {
                qbRtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
            }
        }
    }

    private class QBRTCClientSessionCallbacksImpl implements QBRTCClientSessionCallbacks {

        private final String TAG = QBRTCClientSessionCallbacksImpl.class.getSimpleName();

        @Override
        public void onReceiveNewSession(QBRTCSession qbRtcSession) {
            Log.d(TAG, "onReceiveNewSession(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());
            if (currentQbRtcSession != null) {
                Log.d(TAG, "onReceiveNewSession(). Stop new session. Device now is busy");
                if (!qbRtcSession.equals(currentQbRtcSession)) {
                    qbRtcSession.rejectCall(null);
                }
            } else {
                Log.d(TAG, "onReceiveNewSession(). init session.");
                if (activityClass != null) {

                    startCallActivity(qbRtcSession);
                    currentQbRtcSession = qbRtcSession;
                }
            }
        }

        @Override
        public void onUserNotAnswer(QBRTCSession qbRtcSession, Integer integer) {
            Log.d(TAG, "onUserNotAnswer(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onUserNotAnswer(qbRtcSession, integer);
            }
        }

        @Override
        public void onCallRejectByUser(QBRTCSession qbRtcSession, Integer integer, Map<String, String> map) {
            Log.d(TAG, "onCallRejectByUser(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onCallRejectByUser(qbRtcSession, integer, map);
            }
        }

        @Override
        public void onCallAcceptByUser(QBRTCSession qbRtcSession, Integer integer, Map<String, String> map) {
            Log.d(TAG, "onCallAcceptByUser(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onCallAcceptByUser(qbRtcSession, integer, map);
            }
        }

        @Override
        public void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map) {
            Log.d(TAG,
                    "onReceiveHangUpFromUser(), qbRtcSession.getSession() = " + qbrtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onReceiveHangUpFromUser(qbrtcSession, integer, map);
            }
        }

        @Override
        public void onUserNoActions(QBRTCSession qbRtcSession, Integer integer) {
            Log.d(TAG, "onUserNoActions(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onUserNoActions(qbRtcSession, integer);
            }
        }

        @Override
        public void onSessionClosed(QBRTCSession qbRtcSession) {
            Log.d(TAG, "onSessionClosed(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onSessionClosed(qbRtcSession);
            }
        }

        @Override
        public void onSessionStartClose(QBRTCSession qbRtcSession) {
            Log.d(TAG, "onSessionStartClose(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());
            CoreSharedHelper.getInstance().saveLastOpenActivity(null);

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onSessionStartClose(qbRtcSession);
            }
        }
    }

    class CameraErrorHandler implements CameraVideoCapturer.CameraEventsHandler {

        @Override
        public void onCameraError(String s) {
            Log.e(TAG, "Error on cams, error = " + s);
        }

        @Override
        public void onCameraDisconnected() {

        }

        @Override
        public void onCameraFreezed(String s) {
            Log.e(TAG, "Camera is frozen " + s);
        }

        @Override
        public void onCameraOpening(String i) {
            Log.e(TAG, "Camera opening = " + i);
        }

        @Override
        public void onFirstFrameAvailable() {
            Log.e(TAG, "Camera first frame available");
        }

        @Override
        public void onCameraClosed() {
            Log.e(TAG, "Camera closed");
        }
    }
}