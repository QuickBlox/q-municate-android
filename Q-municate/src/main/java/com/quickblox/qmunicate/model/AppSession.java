package com.quickblox.qmunicate.model;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.io.Serializable;

public class AppSession implements Serializable {

    private static AppSession activeSession;
    private final LoginType loginType;
    private static final Object lock = new Object();
    private final int userId;

    private AppSession(LoginType loginType, int userId) {
        this.loginType = loginType;
        this.userId = userId;
        save();
    }

    public static void startSession(LoginType loginType, int userId) {
        activeSession = new AppSession(loginType, userId);
    }

    public void closeAndClear() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_LOGIN_TYPE);
        activeSession = null;
    }

    public void save() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_LOGIN_TYPE, loginType.toString());
        prefsHelper.savePref(PrefsHelper.PREF_USER_ID, userId);
    }

    public static AppSession getActiveSession() {
        synchronized (lock) {
            return activeSession;
        }
    }

    public boolean isSessionExist() {
        return loginType != null && userId != Consts.NOT_INITIALIZED_VALUE;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public static AppSession load() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        String loginTypeRaw = helper.getPref(PrefsHelper.PREF_LOGIN_TYPE, LoginType.EMAIL.toString());
        int userId = helper.getPref(PrefsHelper.PREF_USER_ID, Consts.NOT_INITIALIZED_VALUE);
        LoginType loginType = LoginType.valueOf(loginTypeRaw);
        return new AppSession(loginType, userId);
    }
}
