package com.quickblox.q_municate.model;

import android.text.TextUtils;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.PrefsHelper;

import java.io.Serializable;

public class AppSession implements Serializable {

    private static AppSession activeSession;
    private final LoginType loginType;
    private static final Object lock = new Object();
    private QBUser user;
    private String sessionToken;

    private AppSession(LoginType loginType, QBUser user, String sessionToken) {
        this.loginType = loginType;
        this.user = user;
        this.sessionToken = sessionToken;
        save();
    }

    public static void startSession(LoginType loginType, QBUser user, String sessionToken) {
        activeSession = new AppSession(loginType, user, sessionToken);
    }

    public void closeAndClear() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_LOGIN_TYPE);
        helper.delete(PrefsHelper.PREF_SESSION_TOKEN);
        helper.delete(PrefsHelper.PREF_USER_ID);
        activeSession = null;
    }

    public QBUser getUser() {
        return user;
    }

    public void save() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_LOGIN_TYPE, loginType.toString());
        prefsHelper.savePref(PrefsHelper.PREF_SESSION_TOKEN, sessionToken);
        saveUser(user, prefsHelper);
    }

    private static AppSession getActiveSession() {
        synchronized (lock) {
            return activeSession;
        }
    }

    public void updateUser(QBUser user) {
        this.user = user;
        saveUser(this.user, App.getInstance().getPrefsHelper());
    }

    private void saveUser(QBUser user, PrefsHelper prefsHelper) {
        prefsHelper.savePref(PrefsHelper.PREF_USER_ID, user.getId());
        prefsHelper.savePref(PrefsHelper.PREF_USER_EMAIL, user.getEmail());
        prefsHelper.savePref(PrefsHelper.PREF_USER_FULL_NAME, user.getFullName());
        prefsHelper.savePref(PrefsHelper.PREF_USER_PASSWORD, user.getPassword());
    }

    public boolean isSessionExist() {
        return loginType != null && !TextUtils.isEmpty(sessionToken);
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public static AppSession load() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        String loginTypeRaw = helper.getPref(PrefsHelper.PREF_LOGIN_TYPE, LoginType.EMAIL.toString());
        int userId = helper.getPref(PrefsHelper.PREF_USER_ID, Consts.NOT_INITIALIZED_VALUE);
        String userFullName = helper.getPref(PrefsHelper.PREF_USER_FULL_NAME, Consts.EMPTY_STRING);
        String sessionToken = helper.getPref(PrefsHelper.PREF_SESSION_TOKEN, Consts.EMPTY_STRING);
        QBUser qbUser = new QBUser();
        qbUser.setId(userId);
        qbUser.setFullName(userFullName);
        LoginType loginType = LoginType.valueOf(loginTypeRaw);
        return new AppSession(loginType, qbUser, sessionToken);
    }

    public static void saveRememberMe(boolean value) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_REMEMBER_ME, value);
    }

    public static AppSession getSession() {
        AppSession activeSession = AppSession.getActiveSession();
        if (activeSession == null) {
            activeSession = AppSession.load();
        }
        return activeSession;
    }
}
