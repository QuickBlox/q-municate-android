package com.quickblox.q_municate.ui.mediacall;

/**
 * Created by tereha on 07.04.15.
 */

public interface OutgoingCallFragmentInterface {

    /**
     * On/off device microphone depends on argument
     *
     * @param turnOn - if true - turn microphone on or turn it off
     */
    void onMic(boolean turnOn);

    /**
     * On/off device camera depends on argument
     *
     * @param turnOn - if true - turn camera on or turn it off
     */
    void onCam(boolean turnOn);

    /**
     * Switching between front and back cameras
     */
    void switchCam();

    /**
     * Switching between headphone speaker and device one
     */
    void switchSpeaker();

    /**
     * Stop conversation
     */
    void hangUpClick();

    /**
     * On local video view start creating
     */
    void onLocalVideoViewCreated();

    /**
     * On remote video view start creating
     */
    void onRemoteVideoViewCreated();
}
