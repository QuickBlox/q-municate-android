package com.quickblox.qmunicate;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.quickblox.core.QBSettings;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private static App instance;

    private PrefsHelper prefsHelper;
    private QBUser user;
    private List<Friend> friends;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initAppication();
    }

    public void initImageLoader(Context context) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565).preProcessor(new ScaleBitmapPreProcessor()).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new HashCodeFileNameGeneratorWithOutToken())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                // TODO IS Remove for release app
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public PrefsHelper getPrefsHelper() {
        return prefsHelper;
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
        QBSettings.getInstance().fastConfigInit(Consts.QB_APP_ID, Consts.QB_AUTH_KEY, Consts.QB_AUTH_SECRET);
        friends = new ArrayList<Friend>();
        prefsHelper = new PrefsHelper(this);
    }

    private class ScaleBitmapPreProcessor implements BitmapProcessor {

        @Override
        public Bitmap process(Bitmap bitmap) {
            if (bitmap.getHeight() * bitmap.getWidth() > Consts.UIL_MAX_BITMAP_SIZE) {
                Bitmap result = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2,
                        bitmap.getHeight() / 2, false);
                bitmap.recycle();
                return result;
            } else {
                return bitmap;
            }
        }
    }

    private class HashCodeFileNameGeneratorWithOutToken extends HashCodeFileNameGenerator {

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
