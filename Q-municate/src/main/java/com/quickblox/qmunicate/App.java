package com.quickblox.qmunicate;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;
import com.quickblox.core.QBSettings;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    public static final int MAX_BITMAP_SIZE = 2000000;

    private static final String APP_ID = "7232";
    private static final String AUTH_KEY = "MpOecRZy-5WsFva";
    private static final String AUTH_SECRET = "dTSLaxDsFKqegD7";

    private static App instance;

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
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .preProcessor(new ScaleBitmapPreProcessor())
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                        // TODO Remove for release app
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public SharedPreferences getSharedPreferences() {
        return getSharedPreferences(App.class.getSimpleName(), Context.MODE_PRIVATE);
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
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        friends = new ArrayList<Friend>();
    }

    private class ScaleBitmapPreProcessor implements BitmapProcessor {
        @Override
        public Bitmap process(Bitmap bitmap) {
            if (bitmap.getHeight() * bitmap.getWidth() > MAX_BITMAP_SIZE) {
                Bitmap result = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / 2, bitmap.getHeight() / 2, false);
                bitmap.recycle();
                return result;
            } else {
                return bitmap;
            }
        }
    }
}
