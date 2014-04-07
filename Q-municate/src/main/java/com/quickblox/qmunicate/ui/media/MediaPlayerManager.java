package com.quickblox.qmunicate.ui.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.provider.Settings;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.utils.ErrorUtils;

public class MediaPlayerManager {
    private static final int INVALID_SOURCE = R.string.error_playing_ringtone;

    private MediaPlayer player;
    private Context context;

    private boolean isPlaying;
    private int originalVolume;

    public MediaPlayerManager(Context context) {
        this.context = context;
    }

    public void playSound(String resource, boolean looping) {
        AssetsSoundResource assetsSoundResource = new AssetsSoundResource(resource, context);
        playResource(assetsSoundResource, true, true);
    }

    public void playDefaultRingTone() {
        UriSoundResource uriSoundResource = new UriSoundResource(Settings.System.DEFAULT_RINGTONE_URI, context);
        playResource(uriSoundResource, true, true);
    }

    public void setMaxVolume() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
    }

    public void returnOriginalVolume() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
    }

    public void stopPlaying() {
        if (isPlaying) {
            if (player != null && player.isPlaying()) {
                player.stop();
            }
            shutDown();
            returnOriginalVolume();
        }
        isPlaying = false;
    }

    private void shutDown() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void playDefault() {
        AssetsSoundResource assetsSoundResource = new AssetsSoundResource("defaultRingtone.ogg", context);
        playResource(assetsSoundResource, true, false);
    }

    private void playResource(SoundResource resource, boolean looping, boolean catchException) {
        int errorId = 0;
        stopPlaying();
        player = new MediaPlayer();
        try {
            setMaxVolume();
            player.setLooping(looping);
            resource.putResourceInPlayer(player);
            player.prepare();
            player.start();
            isPlaying = true;
        } catch (Exception e) {
            errorId = INVALID_SOURCE;
            e.printStackTrace();
        }
        if (errorId != 0) {
            if (!catchException) {
                ErrorUtils.showError(context, context.getString(errorId));
            } else {
                playDefault();
            }
        }
    }
}
