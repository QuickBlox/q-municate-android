package com.quickblox.q_municate;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate.utils.media.MediaPlayerManager;
import com.quickblox.q_municate.utils.ActivityLifecycleHandler;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.helpers.SharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;

public class App extends MultiDexApplication {

    private static App instance;
    private MediaPlayerManager soundPlayer;
    private SharedHelper appSharedHelper;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApplication();
        registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());
    }

    private void initApplication() {
        instance = this;

        initQb(this);
        initDb();
        initImageLoader(this);
    }

    private void initQb(Context context) {
        QBChatService.setDebugEnabled(context.getResources().getBoolean(R.bool.qb_debug));
        QBSettings.getInstance().fastConfigInit(context.getString(R.string.qb_app_id), context.getString(
                R.string.qb_auth_key), context.getString(R.string.qb_secret));
    }

    private void initDb() {
        DataManager.init(this);
    }

    private void initImageLoader(Context context) {
        ImageLoader.getInstance().init(ImageUtils.getImageLoaderConfiguration(context));
    }

    public synchronized MediaPlayerManager getMediaPlayer() {
        return soundPlayer == null
                ? soundPlayer = new MediaPlayerManager(this)
                : soundPlayer;
    }

    public synchronized SharedHelper getAppSharedHelper() {
        return appSharedHelper == null
                ? appSharedHelper = new SharedHelper(this)
                : appSharedHelper;
    }
}