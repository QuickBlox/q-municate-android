package com.quickblox.q_municate_core.qb.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.q_municate_core.models.CallType;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSessionConnectionCallbacks;
import com.quickblox.videochat.webrtc.callbacks.QBRTCSignalingCallback;

import org.webrtc.VideoCapturerAndroid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QBCallChatHelper extends BaseHelper {

    private static final String TAG = QBCallChatHelper.class.getSimpleName();

    private static final int ANSWER_INTERVAL = 30;

    private QBChatService qbChatService;
    private QBRTCClient qbRtcClient;
    private Class<? extends Activity> activityClass;

    private QBRTCSession currentQbRtcSession;
    private boolean isClientClosed;
    private QBRTCClientSessionCallbacks qbRtcClientSessionCallbacks;

    public QBCallChatHelper(Context context) {
        super(context);
    }

    public void init(QBChatService chatService) {
        Log.d(TAG, "init()");

        this.qbChatService = chatService;
        qbRtcClient = QBRTCClient.getInstance(context);

        this.qbChatService.getVideoChatWebRTCSignalingManager().addSignalingManagerListener(new QBVideoChatSignalingManagerListenerImpl());

        qbRtcClient.addSessionCallbacksListener(new QBRTCClientSessionCallbacksImpl());

        setUpCallClient();
    }

    public void initActivityClass(Class<? extends Activity> activityClass) {
        Log.d(TAG, "initActivityClass()");
        this.activityClass = activityClass;
    }

    public QBRTCSession getCurrentRtcSession() {
        return currentQbRtcSession;
    }

    public void initCurrentSession(QBRTCSession qbRtcSession, QBRTCSignalingCallback qbRtcSignalingCallback, QBRTCSessionConnectionCallbacks qbRtcSessionConnectionCallbacks) {
        this.currentQbRtcSession = qbRtcSession;
        initCurrentSession(qbRtcSignalingCallback, qbRtcSessionConnectionCallbacks);
    }

    public void initCurrentSession(QBRTCSignalingCallback qbRtcSignalingCallback, QBRTCSessionConnectionCallbacks qbRtcSessionConnectionCallbacks) {
        this.currentQbRtcSession.addSignalingCallback(qbRtcSignalingCallback);
        this.currentQbRtcSession.addSessionCallbacksListener(qbRtcSessionConnectionCallbacks);
    }

    public void releaseCurrentSession(QBRTCSignalingCallback qbRtcSignalingCallback, QBRTCSessionConnectionCallbacks qbRtcSessionConnectionCallbacks) {
        this.currentQbRtcSession.removeSignalingCallback(qbRtcSignalingCallback);
        this.currentQbRtcSession.removeSessionnCallbacksListener(qbRtcSessionConnectionCallbacks);
        this.currentQbRtcSession = null;
    }

    private void setUpCallClient() {
        Log.d(TAG, "setUpCallClient()");

        isClientClosed = false;

        QBRTCConfig.setAnswerTimeInterval(ANSWER_INTERVAL);

        qbRtcClient.setCameraErrorHendler(new VideoCapturerAndroid.CameraErrorHandler() {
            @Override
            public void onCameraError(String error) {
                Log.e(TAG, "Error on cams, error = " + error);
            }
        });

        QBRTCConfig.setMaxOpponentsCount(6);
        QBRTCConfig.setDisconnectTime(30);
        QBRTCConfig.setAnswerTimeInterval(30l);
        QBRTCConfig.setDebugEnabled(true);

        qbRtcClient.prepareToProcessCalls();
    }

    private void startCallActivity(QBRTCSession qbRtcSession) {
        User user = DataManager.getInstance().getUserDataManager().get(qbRtcSession.getSessionDescription().getCallerID());

        Log.d(TAG, "startCallActivity(), user = " + user);

        List<QBUser> qbUsersList = new ArrayList<>(1);
        qbUsersList.add(UserFriendUtils.createQbUser(user));

        Intent intent = new Intent(context, activityClass);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENTS, (Serializable) qbUsersList);
        intent.putExtra(QBServiceConsts.EXTRA_CALL_TYPE, CallType.INCOMING);
        intent.putExtra(QBServiceConsts.EXTRA_CONFERENCE_TYPE, qbRtcSession.getConferenceType());
        intent.putExtra(QBServiceConsts.EXTRA_SESSION_DESCRIPTION, qbRtcSession.getSessionDescription());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.getApplicationContext().startActivity(intent);
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
                Log.d(TAG, "SimpleSignallingManagerListener. QBCallChatHelper. New QBSignaling was created " + qbSignaling);
                if (isClientClosed) {
                    Log.d(TAG, "ReInit RTCClient");
                    setUpCallClient();
                }
                qbRtcClient.addSignaling((QBWebRTCSignaling) qbSignaling);
            } else {
                Log.d(TAG,
                        "SimpleSignallingManagerListener. QBCallChatHelper. New QBSignaling was not created " + qbSignaling);
            }
        }
    }

    private class QBRTCClientSessionCallbacksImpl implements QBRTCClientSessionCallbacks {

        private final String TAG = QBRTCClientSessionCallbacksImpl.class.getSimpleName();

        @Override
        public void onReceiveNewSession(QBRTCSession qbRtcSession) {
            Log.d(TAG, "onReceiveNewSession(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());
            if (getCurrentRtcSession() != null) {
                Log.d(TAG, "onReceiveNewSession(). Stop new session. Device now is busy");
                qbRtcSession.rejectCall(null);
            } else {
                Log.d(TAG, "onReceiveNewSession(). init session.");
                currentQbRtcSession = qbRtcSession;
                startCallActivity(qbRtcSession);
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
        public void onReceiveHangUpFromUser(QBRTCSession qbRtcSession, Integer integer) {
            Log.d(TAG, "onReceiveHangUpFromUser(), qbRtcSession.getSession() = " + qbRtcSession.getSessionID());

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onReceiveHangUpFromUser(qbRtcSession, integer);
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

            if (qbRtcClientSessionCallbacks != null) {
                qbRtcClientSessionCallbacks.onSessionStartClose(qbRtcSession);
            }
        }
    }
}