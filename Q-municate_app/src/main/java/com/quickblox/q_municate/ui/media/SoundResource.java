package com.quickblox.q_municate.ui.media;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;


public abstract class SoundResource<T> {
    protected T resource;
    protected Context context;

    public SoundResource(T resource, Context context) {
        this.resource = resource;
        this.context = context;
    }

    public abstract void putResourceInPlayer(MediaPlayer player) throws
            IllegalArgumentException, IllegalStateException, IOException, SecurityException;
}
