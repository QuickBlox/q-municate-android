package com.quickblox.qmunicate;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.core.QBSettings;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.qmunicate.ui.media.MediaPlayerManager;
import com.quickblox.qmunicate.utils.ActivityLifecycleHandler;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ImageHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    private static App instance;

    private PrefsHelper prefsHelper;
    private MediaPlayerManager soundPlayer;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
        registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());
    }

    private void initImageLoader(Context context) {
        ImageLoader.getInstance().init(ImageHelper.getImageLoaderConfiguration(context));
    }

    public PrefsHelper getPrefsHelper() {
        return prefsHelper;
    }

    public MediaPlayerManager getMediaPlayer() {
        return soundPlayer;
    }

    private void initApplication() {
        instance = this;
        QBChatService.setDebugEnabled(true);
        initImageLoader(this);
        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
        prefsHelper = new PrefsHelper(this);
        soundPlayer = new MediaPlayerManager(this);
    }
}