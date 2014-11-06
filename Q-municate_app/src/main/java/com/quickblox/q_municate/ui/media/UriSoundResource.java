package com.quickblox.q_municate.ui.media;


import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

import java.io.IOException;

public class UriSoundResource extends SoundResource<Uri> {

    public UriSoundResource(Uri resource, Context context) {
        super(resource, context);
    }

    @Override
    public void putResourceInPlayer(MediaPlayer player) throws IllegalArgumentException, IllegalStateException, IOException {
        player.setDataSource(context, resource);
    }
}
