package com.quickblox.q_municate.ui.mediacall;

/**
 * Created on 7/8/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public interface CallVideoActionsListener {

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


}
