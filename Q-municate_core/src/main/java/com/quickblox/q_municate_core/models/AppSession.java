package com.quickblox.q_municate_core.models;

import android.text.TextUtils;

import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;
import com.quickblox.auth.QBAuth;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.users.model.QBUser;

import java.io.Serializable;
import java.util.Date;

public class AppSession implements Serializable {

    private static final Object lock = new Object();
    private static AppSession activeSession;

    private CoreSharedHelper coreSharedHelper;
    private final LoginType loginType;
    private QBUser qbUser;
    private String qbToken;

    private AppSession(LoginType loginType, QBUser qbUser, String qbToken) {
        coreSharedHelper = CoreSharedHelper.getInstance();
        this.loginType = loginType;
        this.qbUser = qbUser;
        this.qbToken = qbToken;
        save();
    }

    public static void startSession(LoginType loginType, QBUser user, String qbToken) {
        activeSession = new AppSession(loginType, user, qbToken);
    }

    private static AppSession getActiveSession() {
        synchronized (lock) {
            return activeSession;
        }
    }

    public static AppSession load() {
        String loginTypeRaw = CoreSharedHelper.getInstance().getLoginType();
        String qbToken = CoreSharedHelper.getInstance().getQBToken();

        int userId = CoreSharedHelper.getInstance().getUserId();
        String userFullName = CoreSharedHelper.getInstance().getUserFullName();

        QBUser qbUser = new QBUser();
        qbUser.setId(userId);
        qbUser.setEmail(CoreSharedHelper.getInstance().getUserEmail());
        qbUser.setPassword(CoreSharedHelper.getInstance().getUserPassword());
        qbUser.setFullName(userFullName);

        LoginType loginType = LoginType.valueOf(loginTypeRaw);

        return new AppSession(loginType, qbUser, qbToken);
    }

    public static boolean isSessionExistOrNotExpired(long expirationTime) {
        try {
            BaseService baseService = QBAuth.getBaseService();
            String token = baseService.getToken();
            if (token == null) {
                return false;
            }
            Date tokenExpirationDate = baseService.getTokenExpirationDate();
            long tokenLiveOffset = tokenExpirationDate.getTime() - System.currentTimeMillis();
            return tokenLiveOffset > expirationTime;
        } catch (BaseServiceException e) {
            // nothing by default
        }
        return false;
    }

    public static AppSession getSession() {
        AppSession activeSession = AppSession.getActiveSession();
        if (activeSession == null) {
            activeSession = AppSession.load();
        }
        return activeSession;
    }

    public void closeAndClear() {
        coreSharedHelper.saveQBToken(null);
        coreSharedHelper.saveLoginType(null);

        coreSharedHelper.clearUserData();

        activeSession = null;
    }

    public QBUser getUser() {
        return qbUser;
    }

    public void save() {
        coreSharedHelper.saveQBToken(qbToken);
        coreSharedHelper.saveLoginType(loginType.toString());

        saveUser(qbUser);
    }

    public void updateUser(QBUser qbUser) {
        this.qbUser = qbUser;
        saveUser(this.qbUser);
    }

    private void saveUser(QBUser user) {
        coreSharedHelper.saveUserId(user.getId());
        coreSharedHelper.saveUserEmail(user.getEmail());
        coreSharedHelper.saveUserPassword(user.getPassword());
        coreSharedHelper.saveUserFullName(user.getFullName());
    }

    public boolean isLoggedIn() {
        return qbUser != null && !TextUtils.isEmpty(qbToken);
    }

    public boolean isSessionExist() {
        return loginType != null && !TextUtils.isEmpty(qbToken);
    }

    public LoginType getLoginType() {
        return loginType;
    }
}