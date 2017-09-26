package com.quickblox.q_municate_core.models;

import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.auth.session.QBSessionParameters;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.users.model.QBUser;

import java.io.Serializable;
import java.util.Date;

public class AppSession implements Serializable {

    private static final Object lock = new Object();
    private static AppSession activeSession;

    private CoreSharedHelper coreSharedHelper;
    private LoginType loginType;
    private QBUser qbUser;

    private ChatState chatState = ChatState.FOREGROUND;

    private AppSession(QBUser qbUser) {
        coreSharedHelper = CoreSharedHelper.getInstance();
        this.qbUser = qbUser;
        this.loginType = getLoginTypeBySessionParameters(QBSessionManager.getInstance().getSessionParameters());
        save();
    }

    public void updateState(ChatState state){
        chatState = state;
    }

    public ChatState getChatState() {
        return chatState;
    }

    public static void startSession(QBUser user) {
        activeSession = new AppSession(user);
    }

    private static AppSession getActiveSession() {
        synchronized (lock) {
            return activeSession;
        }
    }

    public static AppSession load() {

        int userId = CoreSharedHelper.getInstance().getUserId();
        String userFullName = CoreSharedHelper.getInstance().getUserFullName();

        QBUser qbUser = new QBUser();
        qbUser.setId(userId);
        qbUser.setEmail(CoreSharedHelper.getInstance().getUserEmail());
        qbUser.setPassword(CoreSharedHelper.getInstance().getUserPassword());
        qbUser.setFullName(userFullName);
        qbUser.setFacebookId(CoreSharedHelper.getInstance().getFBId());
        qbUser.setTwitterId(CoreSharedHelper.getInstance().getTwitterId());
        qbUser.setTwitterDigitsId(CoreSharedHelper.getInstance().getTwitterDigitsId());
        qbUser.setCustomData(CoreSharedHelper.getInstance().getUserCustomData());

        activeSession = new AppSession(qbUser);

        return activeSession;
    }

    public static boolean isSessionExistOrNotExpired(long expirationTime) {
            QBSessionManager qbSessionManager = QBSessionManager.getInstance();
            String token = qbSessionManager.getToken();
            if (token == null) {
                Log.d("AppSession", "token == null");
                return false;
            }
            Date tokenExpirationDate = qbSessionManager.getTokenExpirationDate();
            long tokenLiveOffset = tokenExpirationDate.getTime() - System.currentTimeMillis();
            return tokenLiveOffset > expirationTime;
    }

    public static AppSession getSession() {
        AppSession activeSession = AppSession.getActiveSession();
        if (activeSession == null) {
            activeSession = AppSession.load();
        }
        return activeSession;
    }

    public void closeAndClear() {
        coreSharedHelper.clearUserData();

        activeSession = null;
    }

    public QBUser getUser() {
        return qbUser;
    }

    public void save() {
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
        coreSharedHelper.saveFBId(user.getFacebookId());
        coreSharedHelper.saveTwitterId(user.getTwitterId());
        coreSharedHelper.saveTwitterDigitsId(user.getTwitterDigitsId());
        coreSharedHelper.saveUserCustomData(user.getCustomData());
    }

    public boolean isLoggedIn() {
        return QBSessionManager.getInstance().getSessionParameters() != null;
    }

    public boolean isSessionExist() {
        return !TextUtils.isEmpty(QBSessionManager.getInstance().getToken());
    }

    public LoginType getLoginType() {
        return loginType;
    }

    private LoginType getLoginTypeBySessionParameters(QBSessionParameters sessionParameters){
        LoginType result = null;
        if(sessionParameters == null){
            return null;
        }
        String socialProvider = sessionParameters.getSocialProvider();
        if(socialProvider == null){
            result = LoginType.EMAIL;
        } else if (socialProvider.equals(QBProvider.FACEBOOK)){
            result = LoginType.FACEBOOK;
        } else if (socialProvider.equals(QBProvider.FIREBASE_PHONE)){
            result = LoginType.FIREBASE_PHONE;
        } else if (socialProvider.equals(QBProvider.TWITTER_DIGITS)){ //for correct migration from TWITTER_DIGITS to FIREBASE_PHONE
            result = LoginType.FIREBASE_PHONE;
        }

        if (result != null) {
            loginType = result;
        }

        return result;
    }

    public enum ChatState {
        BACKGROUND, FOREGROUND
    }

}