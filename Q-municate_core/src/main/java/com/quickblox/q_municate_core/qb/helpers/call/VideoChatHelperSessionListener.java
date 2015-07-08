package com.quickblox.q_municate_core.qb.helpers.call;
import com.quickblox.videochat.webrtc.view.QBRTCVideoTrack;

import java.util.Map;

/**
 * Created on 4/16/15.
 *
 * Interface send callbacks from {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper},
 * related with {@link com.quickblox.videochat.webrtc.QBRTCSession} lifecycle.
 * Each of callbacks thrown to {@link com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper}
 * by {@link com.quickblox.videochat.webrtc.QBRTCClient}
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public interface VideoChatHelperSessionListener {

    /**
     * List of {@link com.quickblox.videochat.webrtc.callbacks.QBRTCClientSessionCallbacks}
     */

    void onReceiveNewSession();

    void onUserNotAnswer(Integer opponentID);

    void onCallRejectByUser(Integer integer, Map<String, String> userInfo);

    void onReceiveHangUpFromUser(Integer opponentID);

    void onSessionClosed();

    void onSessionStartClose();

    void onError(String s);



}
