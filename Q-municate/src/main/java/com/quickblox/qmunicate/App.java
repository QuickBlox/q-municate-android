package com.quickblox.qmunicate;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.quickblox.core.QBSettings;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.qmunicate.ui.media.MediaPlayerManager;
import com.quickblox.qmunicate.utils.ActivityLifecycleHandler;
import com.quickblox.qmunicate.utils.Consts;
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

    public void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(Consts.UIL_DEFAULT_DISPLAY_OPTIONS)
                .denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(
                        new HashCodeFileNameGeneratorWithoutToken())
                        // TODO IS Remove for release app
                .writeDebugLogs().build();
        ImageLoader.getInstance().init(config);
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

    private class HashCodeFileNameGeneratorWithoutToken extends HashCodeFileNameGenerator {

        private static final String FACEBOOK_PATTERN = "https://graph.facebook.com/";
        private static final String TOKEN_PATTERN = "\\?token+=+.*";

        @Override
        public String generate(String imageUri) {
            if (imageUri.contains(FACEBOOK_PATTERN)) {
                return imageUri;
            }
            String replace = imageUri.replaceAll(TOKEN_PATTERN, "");
            return super.generate(replace);
        }
    }
}