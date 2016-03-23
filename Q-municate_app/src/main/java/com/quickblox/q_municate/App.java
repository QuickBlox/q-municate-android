package com.quickblox.q_municate;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBSettings;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.ActivityLifecycleHandler;
import com.quickblox.q_municate.utils.helpers.SharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;

public class App extends MultiDexApplication {

    private static App instance;
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

        initQb();
        initDb();
        initImageLoader(this);
    }

    private void initQb() {
        QBChatService.setDebugEnabled(StringObfuscator.getDebugEnabled());
        QBSettings.getInstance().fastConfigInit(
                StringObfuscator.getApplicationId(),
                StringObfuscator.getAuthKey(),
                StringObfuscator.getAuthSecret());
    }

    private void initDb() {
        DataManager.init(this);
    }

    private void initImageLoader(Context context) {
        ImageLoader.getInstance().init(ImageLoaderUtils.getImageLoaderConfiguration(context));
    }

    public synchronized SharedHelper getAppSharedHelper() {
        return appSharedHelper == null
                ? appSharedHelper = new SharedHelper(this)
                : appSharedHelper;
    }
}