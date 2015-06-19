package com.quickblox.q_municate_core.qb.helpers.call;

import android.util.Log;

import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCSessionDescription;
import com.quickblox.videochat.webrtc.callbacks.SmackSignallingProcessorCallback;

import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import java.util.List;

/**
 * Created by PC on 19.05.2015.
 */
public class SmackSignallingProcessorCallbackImpl implements SmackSignallingProcessorCallback{

    private static final String TAG = SmackSignallingProcessorCallbackImpl.class.getSimpleName();

    @Override
    public void onReceiveCallFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription, SessionDescription sessionDescription) {
            Log.d(TAG, "onReceiveCallFromUser");
    }

    @Override
    public void onReceiveAcceptFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription, SessionDescription sessionDescription) {
        Log.d(TAG, "onReceiveAcceptFromUser");
    }

    @Override
    public void onReceiveRejectFromUser(Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
        Log.d(TAG, "onReceiveRejectFromUser");
    }

    @Override
    public void onReceiveIceCandidatesFromUser(List<IceCandidate> iceCandidates, Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
        Log.d(TAG, "onReceiveIceCandidatesFromUser");
    }

    @Override
    public void onReceiveUserHungUpCall(Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
        Log.d(TAG, "onReceiveUserHungUpCall");
    }

    @Override
    public void onAddUserNeed(Integer integer, QBRTCSessionDescription qbrtcSessionDescription) {
        Log.d(TAG, "onAddUserNeed");
    }
}
