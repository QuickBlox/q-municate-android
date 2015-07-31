package com.quickblox.q_municate.ui.mediacall;

/**
 * Created on 7/8/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public interface CallAudioActionsListener {

    /**
     * On/off device microphone depends on argument
     *
     * @param turnOn - if true - turn microphone on or turn it off
     */
    void onMic(boolean turnOn);


    /**
     * Switching between headphone speaker and device one
     *
     * There are two variants of switching audio outputs:
     * If earphones plugged in then switching will be executed between earphones and device speaker
     * If earphones are absent then switching will be executed between device's earphone and speaker
     */
    void switchSpeaker();

}
