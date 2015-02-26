package com.quickblox.q_municate;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate.ui.media.MediaPlayerManager;
import com.quickblox.q_municate.utils.ActivityLifecycleHandler;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.ImageUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_db.managers.DatabaseManager;

public class App extends Application {

    private static App instance;
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
        ImageLoader.getInstance().init(ImageUtils.getImageLoaderConfiguration(context));
    }

    private void initDB(Context context) {
        DatabaseManager.init(this);
    }

    public MediaPlayerManager getMediaPlayer() {
        return soundPlayer;
    }

    private void initApplication() {
        instance = this;
        QBChatService.setDebugEnabled(true);
        initImageLoader(this);
        initDB(this);
        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY,
                Consts.QB_AUTH_SECRET);
        soundPlayer = new MediaPlayerManager(this);
        new PrefsHelper(this);
    }
}