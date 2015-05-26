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

    void onReceiveNewSession();

    void onUserNotAnswer(Integer integer);

    void onCallRejectByUser(Integer integer, Map<String, String> map);

    void onReceiveHangUpFromUser(Integer integer);

    void onSessionClosed();

    void onSessionStartClose();

    void onLocalVideoTrackReceive(QBRTCVideoTrack videoTrack);

    void onRemoteVideoTrackReceive(QBRTCVideoTrack videoTrack, Integer userID);

    void onClientReady();

    void onError(String s);
}
