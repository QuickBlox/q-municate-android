package com.quickblox.q_municate_core.qb.helpers.call;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.Map;

/**
 * Created on 4/16/15.
 *
 * Interface redirects callbacks from {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper},
 * which was thrown to it by {@link com.quickblox.videochat.webrtc.QBRTCClient}
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public interface VideoChatHelperListener {

    /**
     * List of {@link com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks}
     */

    void onReceiveNewSession();

    void onUserNotAnswer(Integer integer);

    void onCallRejectByUser(Integer integer, Map<String, String> map);

    void onReceiveHangUpFromUser(Integer integer);

    void onSessionClosed();

    void onSessionStartClose();

    void onError(String s);


    /**
     * List of {@link com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks}
     */

    void onLocalVideoTrackReceive(QBRTCVideoTrack videoTrack);

    void onRemoteVideoTrackReceive(QBRTCVideoTrack videoTrack, Integer userID);

    /**
     * Additional callback for notifying UI components that QBRTCClient starts process calls and current session exists.
     * It's necessary when we take decision store call tasks in queue of waiting tasks or start execute it directly on
     * QBVideoChatHelper instance
     */

    void onClientReady();
}
