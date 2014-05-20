package com.quickblox.qmunicate;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.quickblox.core.QBSettings;
import com.quickblox.core.TransferProtocol;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.ui.media.MediaPlayerManager;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    private static App instance;

    private PrefsHelper prefsHelper;
    private QBUser user;
    private MediaPlayerManager soundPlayer;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        initApplication();
    }

    public void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(Consts.UIL_DEFAULT_DISPLAY_OPTIONS)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new HashCodeFileNameGeneratorWithoutToken())
                        // TODO IS Remove for release app
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public PrefsHelper getPrefsHelper() {
        return prefsHelper;
    }

    public MediaPlayerManager getMediaPlayer() {
        return soundPlayer;
    }

    public QBUser getUser() {
        return user;
    }

    public void setUser(QBUser user) {
        this.user = user;
    }

    public LoginType getUserLoginType() {
        int defValue = LoginType.EMAIL.ordinal();
        int value = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_LOGIN_TYPE, defValue);
        return LoginType.values()[value];
    }

    private void initApplication() {
        instance = this;
        QBChatService.setDebugEnabled(true);
        initImageLoader(this);

        // TODO temp
        QBSettings.getInstance().setServerApiDomain("api.stage.quickblox.com");
        QBSettings.getInstance().setChatServerDomain("chatstage.quickblox.com");
        QBSettings.getInstance().setContentBucketName("blobs-test-oz");
        QBSettings.getInstance().setTransferProtocol(TransferProtocol.HTTP);
        //

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