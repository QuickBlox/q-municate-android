package com.quickblox.q_municate_core.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

public class MediaUtils {

    public static boolean isMicrophoneMuted(Activity activity) {
        AudioManager audioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.isMicrophoneMute();
    }

    public static MetaData getMetaData(String localPath) {
        int height = 0;
        int width = 0;
        int durationSec = 0;

        MediaMetadataRetriever mmr = getMediaMetadataRetriever(localPath);
        String heightStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String widthStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

        if (!TextUtils.isEmpty(heightStr)) {
            height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        }
        if (!TextUtils.isEmpty(widthStr)) {
            width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
        }
        if (!TextUtils.isEmpty(durationStr)) {
            int durationMs = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            durationSec = (int) TimeUnit.MILLISECONDS.toSeconds(durationMs);
        }

        return new MetaData(height, width, durationSec);
    }

    private static MediaMetadataRetriever getMediaMetadataRetriever(String localPath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(localPath);
        return mmr;
    }

    public static class MetaData {
        private int videoHeight;
        private int videoWidth;
        private int durationSec;

        MetaData(int videoHeight, int videoWidth, int durationSec) {
            this.videoHeight = videoHeight;
            this.videoWidth = videoWidth;
            this.durationSec = durationSec;
        }

        public int videoHeight() {
            return videoHeight;
        }

        public int videoWidth() {
            return videoWidth;
        }

        public int durationSec() {
            return durationSec;
        }
    }
}