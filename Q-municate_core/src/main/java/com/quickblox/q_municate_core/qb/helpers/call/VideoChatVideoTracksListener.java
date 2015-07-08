package com.quickblox.q_municate_core.qb.helpers.call;

import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

/**
 * Created on 7/8/15.
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 *
 * Interface send callbacks from {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper},
 * related with {@link QBRTCVideoTrack}.
 * Each of callbacks thrown to {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper}
 * by {@link com.quickblox.videochat.webrtc.QBRTCClient}
 */

public interface VideoChatVideoTracksListener {

    /**
     * List of {@link com.quickblox.videochat.webrtc.callbacks.QBRTCClientVideoTracksCallbacks}
     */

    void onLocalVideoTrackReceive(QBRTCVideoTrack videoTrack);

    void onRemoteVideoTrackReceive(QBRTCVideoTrack videoTrack, Integer userID);

}
