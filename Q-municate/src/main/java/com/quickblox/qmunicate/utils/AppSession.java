package com.quickblox.qmunicate.utils;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.model.LoginType;

import java.io.Serializable;

public class AppSession implements Serializable {

    private static AppSession INSTANCE;
    private final LoginType loginType;
    private final String userEmail;

    private AppSession(LoginType loginType, String userEmail) {
        this.loginType = loginType;
        this.userEmail = userEmail;
        save();
    }

    public static void startSession(LoginType loginType, String userMail) {
        INSTANCE = new AppSession(loginType, userMail);
    }

    public void clear() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_LOGIN_TYPE);
    }

    public void save() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_LOGIN_TYPE, loginType.toString());
        prefsHelper.savePref(PrefsHelper.PREF_USER_EMAIL, userEmail);
    }

    public static AppSession getActiveSession() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public boolean isSessionExist() {
        return loginType != null && userEmail != null;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public static AppSession load() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        String loginTypeRaw = helper.getPref(PrefsHelper.PREF_LOGIN_TYPE, LoginType.EMAIL.toString());
        String userMail = helper.getPref(PrefsHelper.PREF_USER_EMAIL, null);
        LoginType loginType = LoginType.valueOf(loginTypeRaw);
        return new AppSession(loginType, userMail);
    }
}
