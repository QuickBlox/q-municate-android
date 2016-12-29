package com.quickblox.q_municate;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.digits.sdk.android.Digits;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.ActivityLifecycleHandler;
import com.quickblox.q_municate.utils.helpers.SharedHelper;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_user_cache.QMUserCacheImpl;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

public class App extends MultiDexApplication {

    private static App instance;
    private SharedHelper appSharedHelper;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initFabric();
        initApplication();
        registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());
    }

    private void initFabric(){
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                StringObfuscator.getTwitterConsumerKey(),
                StringObfuscator.getTwitterConsumerSecret());

        Fabric.with(this,
                crashlyticsKit,
                new TwitterCore(authConfig),
                new Digits.Builder().withTheme(R.style.AppTheme).build());
    }

    private void initApplication() {
        instance = this;

        initQb();
        initDb();
        initImageLoader(this);
        initServices();
    }

    private void initQb() {
        QBChatService.setDebugEnabled(StringObfuscator.getDebugEnabled());

        QBSettings.getInstance().init(getApplicationContext(),
                StringObfuscator.getApplicationId(),
                StringObfuscator.getAuthKey(),
                StringObfuscator.getAuthSecret());
        QBSettings.getInstance().setAccountKey(StringObfuscator.getAccountKey());
    }

    private void initDb() {
        DataManager.init(this);
    }

    private void initImageLoader(Context context) {
        ImageLoader.getInstance().init(ImageLoaderUtils.getImageLoaderConfiguration(context));
    }

    private void initServices(){
        QMAuthService.init();
        QMUserCache userCache = new QMUserCacheImpl(this);
        QMUserService.init(userCache);
    }

    public synchronized SharedHelper getAppSharedHelper() {
        return appSharedHelper == null
                ? appSharedHelper = new SharedHelper(this)
                : appSharedHelper;
    }

}