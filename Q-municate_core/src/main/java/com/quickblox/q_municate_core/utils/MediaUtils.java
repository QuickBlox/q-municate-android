package com.quickblox.q_municate_core.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import java.util.concurrent.TimeUnit;

public class MediaUtils {

    public static boolean isMicrophoneMuted(Activity activity) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isMicrophoneMute();
    }

    public static int getDurationAudioInSec(Context context, String localPath) {
        Uri uri = Uri.parse(localPath);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(context, uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int millSecond = Integer.parseInt(durationStr);
        Long seconds = TimeUnit.MILLISECONDS.toSeconds(millSecond);
        return seconds.intValue();
    }
}