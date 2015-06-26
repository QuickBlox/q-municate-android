package com.quickblox.q_municate.ui.mediacall;

/**
 * Created by tereha on 07.04.15.
 */
//TODO
//onMic() , offMic(); have the same responsibility for one action

//TODO
//Again interface is responsible for many actions
public interface OutgoingCallFragmentInterface {

    void onMic();
    void offMic();
    void onCam();
    void offCam();
    void switchCam();
    void switchSpeaker();
    void hangUpClick();
    void onLocalVideoViewCreated();
    void onRemoteVideoViewCreated();
}
