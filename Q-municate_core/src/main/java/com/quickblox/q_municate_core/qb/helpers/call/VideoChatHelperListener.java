package com.quickblox.q_municate_core.qb.helpers.call;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.Map;

/**
 * Created on 4/16/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public interface VideoChatHelperListener {

    void onReceiveNewSession(QBRTCSession qbrtcSession);

    void onUserNotAnswer(QBRTCSession qbrtcSession, Integer integer);

    void onCallRejectByUser(QBRTCSession qbrtcSession, Integer integer, Map<String, String> map);

    void onReceiveHangUpFromUser(QBRTCSession qbrtcSession, Integer integer);

    void onSessionClosed(QBRTCSession qbrtcSession);

    void onSessionStartClose(QBRTCSession qbrtcSession);

    void onLocalVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack);

    void onRemoteVideoTrackReceive(QBRTCSession session, QBRTCVideoTrack videoTrack, Integer userID);

    void onClientReady();
}
