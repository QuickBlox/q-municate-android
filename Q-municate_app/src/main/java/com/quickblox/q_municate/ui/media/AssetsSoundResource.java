package com.quickblox.q_municate.ui.media;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import com.quickblox.q_municate_core.utils.ConstsCore;

import java.io.IOException;


public class AssetsSoundResource extends SoundResource<String> {

    public AssetsSoundResource(String resource, Context context) {
        super(resource, context);
    }

    @Override
    public void putResourceInPlayer(MediaPlayer player) throws
            IllegalArgumentException, IllegalStateException, IOException {
        AssetFileDescriptor afd = null;
        afd = context.getAssets().openFd(
                ConstsCore.ASSETS_SOUND_PATH + resource);
        player.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(),
                afd.getLength());
    }
}
