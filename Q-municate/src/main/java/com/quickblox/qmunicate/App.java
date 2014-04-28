package com.quickblox.qmunicate;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.quickblox.core.QBSettings;
import com.quickblox.core.TransferProtocol;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.media.MediaPlayerManager;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    private static App instance;

    private PrefsHelper prefsHelper;
    private QBUser user;
    private List<Friend> friends;
    private MediaPlayerManager soundPlayer;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        initAppication();
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

    public List<Friend> getFriends() {
        return friends;
    }

    public void setFriends(List<Friend> friends) {
        this.friends = friends;
    }

    private void initAppication() {
        instance = this;
        initImageLoader(this);

        // TODO temp
        QBSettings.getInstance().setServerApiDomain("api.stage.quickblox.com");
        QBSettings.getInstance().setChatServerDomain("chatstage.quickblox.com");
        QBSettings.getInstance().setContentBucketName("blobs-test-oz");
        QBSettings.getInstance().setTransferProtocol(TransferProtocol.HTTP);
        //

        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
        friends = new ArrayList<Friend>();
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
