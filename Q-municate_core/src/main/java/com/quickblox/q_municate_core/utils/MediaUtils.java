package com.quickblox.q_municate_core.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;

public class MediaUtils {

    public static boolean isMicrophoneMuted(Activity activity) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isMicrophoneMute();
    }
}
