package com.quickblox.qmunicate;

import android.app.Application;

import com.quickblox.core.QBSettings;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class App extends Application {

    private static final String APP_ID = "7232";
    private static final String AUTH_KEY = "MpOecRZy-5WsFva";
    private static final String AUTH_SECRET = "dTSLaxDsFKqegD7";

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
        QBSettings.getInstance().fastConfigInit(APP_ID, AUTH_KEY, AUTH_SECRET);
        friends = new ArrayList<Friend>();
        prefsHelper = new PrefsHelper(this);
    }
}
