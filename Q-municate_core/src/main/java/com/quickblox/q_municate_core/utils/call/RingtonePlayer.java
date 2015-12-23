package com.quickblox.q_municate_core.utils.call;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;

import com.quickblox.q_municate_db.utils.ErrorUtils;

public class RingtonePlayer {

    private static final String TAG = RingtonePlayer.class.getSimpleName();

    private MediaPlayer mediaPlayer;

    public RingtonePlayer(Context context, int resource) {
        mediaPlayer = MediaPlayer.create(context, resource);
    }

    public RingtonePlayer(Context context) {
        Uri notification = getNotification();
        if (notification != null) {
            mediaPlayer = MediaPlayer.create(context, notification);
        }
    }

    private Uri getNotification() {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        if (notification == null) {
            // notification is null, using backup
            notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            // I can't see this ever being null (as always have a default notification)
            // but just in case
            if (notification == null) {
                // notification backup is null, using 2nd backup
                notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            }
        }
        return notification;
    }

    public void play(boolean looping) {
        ErrorUtils.logError(TAG, "play");
        if (mediaPlayer == null) {
            ErrorUtils.logError(TAG, "mediaPlayer isn't created ");
            return;
        }
        mediaPlayer.setLooping(looping);
        mediaPlayer.start();
    }

    public synchronized void stop() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}