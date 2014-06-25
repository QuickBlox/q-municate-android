package com.quickblox.qmunicate.model;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;

import java.io.Serializable;

public class AppSession implements Serializable {

    private static AppSession activeSession;
    private final LoginType loginType;
    private static final Object lock = new Object();
    private static QBUser user;

    private AppSession(LoginType loginType, QBUser user) {
        this.loginType = loginType;
        this.user = user;
        save();
    }

    public static void startSession(LoginType loginType, QBUser user) {
        activeSession = new AppSession(loginType, user);
    }

    public void closeAndClear() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_LOGIN_TYPE);
        activeSession = null;
    }

    public QBUser getUser() {
        return user;
    }

    public void save() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_LOGIN_TYPE, loginType.toString());
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
        prefsHelper.savePref(PrefsHelper.PREF_USER_FULL_NAME, user.getFullName());
    }

    public boolean isSessionExist() {
        return loginType != null && user.getId() != Consts.NOT_INITIALIZED_VALUE;
    }

    public LoginType getLoginType() {
        return loginType;
    }

    public static AppSession load() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        String loginTypeRaw = helper.getPref(PrefsHelper.PREF_LOGIN_TYPE, LoginType.EMAIL.toString());
        int userId = helper.getPref(PrefsHelper.PREF_USER_ID, Consts.NOT_INITIALIZED_VALUE);
        String userFullName = helper.getPref(PrefsHelper.PREF_USER_FULL_NAME, Consts.EMPTY_STRING);
        QBUser qbUser = new QBUser();
        qbUser.setId(userId);
        qbUser.setFullName(userFullName);
        LoginType loginType = LoginType.valueOf(loginTypeRaw);
        return new AppSession(loginType, qbUser);
    }

    public static  void saveRememberMe(boolean value) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_REMEMBER_ME, value);
    }

    public static  void saveUserCredentials(QBUser user) {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.savePref(PrefsHelper.PREF_USER_EMAIL, user.getEmail());
        helper.savePref(PrefsHelper.PREF_USER_PASSWORD, user.getPassword());
    }

    public static AppSession getSession() {
        AppSession activeSession = AppSession.getActiveSession();
        if (activeSession == null) {
            activeSession = AppSession.load();
        }
        return activeSession;
    }
}
