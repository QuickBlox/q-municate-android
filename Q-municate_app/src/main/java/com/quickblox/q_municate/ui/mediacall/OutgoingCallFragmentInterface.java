package com.quickblox.q_municate.ui.mediacall;

import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;

/**
 * Created by tereha on 07.04.15.
 */
public interface OutgoingCallFragmentInterface {

    void onMic();
    void offMic();
    void onCam();
    void offCam();
    void switchCam();
    void switchSpeaker();
    void hungUpClick();
}
