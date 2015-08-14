package com.quickblox.q_municate.ui.authorization;

import android.content.Context;
import android.text.TextUtils;

import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.core.listeners.ExistingQbSessionListener;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoginChatCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.QBSocialLoginCommand;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

import java.util.concurrent.TimeUnit;

public class LoginHelper {

    private Context context;
    private ExistingQbSessionListener existingQbSessionListener;
    private boolean checkedRememberMe;

    public LoginHelper(Context context, ExistingQbSessionListener existingQbSessionListener, boolean checkedRememberMe) {
        this.context = context;
        this.existingQbSessionListener = existingQbSessionListener;
        this.checkedRememberMe = checkedRememberMe;
    }

    public void checkStartExistSession() {
        String userEmail = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_USER_EMAIL);
        String userPassword = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_USER_PASSWORD);
        checkedRememberMe = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, false);

        if (checkedRememberMe) {
            checkStartExistSession(userEmail, userPassword);
        } else {
            existingQbSessionListener.onStartSessionFail();
        }
    }

    public void checkStartExistSession(String userEmail, String userPassword) {
        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);
        if ((isEmailEntered && isPasswordEntered) || (isLoggedViaFB(isPasswordEntered))) {
            runExistSession(userEmail, userPassword);
        } else {
            existingQbSessionListener.onStartSessionFail();
        }
    }

    public boolean isLoggedViaFB(boolean isPasswordEntered) {
        return isPasswordEntered && LoginType.FACEBOOK.equals(getCurrentLoginType());
    }

    public LoginType getCurrentLoginType() {
        return AppSession.getSession().getLoginType();
    }

    public void runExistSession(String userEmail, String userPassword) {
        //check is token valid for about 1 minute
        if (AppSession.isSessionExistOrNotExpired(TimeUnit.MINUTES.toMillis(
                ConstsCore.TOKEN_VALID_TIME_IN_MINUTES))) {
            existingQbSessionListener.onStartSessionSuccess();
        } else {
            doAutoLogin(userEmail, userPassword);
        }
    }

    public void doAutoLogin(String userEmail, String userPassword) {
        if (LoginType.EMAIL.equals(getCurrentLoginType())) {
            login(userEmail, userPassword);
        } else if (LoginType.FACEBOOK.equals(getCurrentLoginType())) {
            loginFB();
        }
    }

    public void loginFB() {
        String tokenFB = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_SESSION_FB_TOKEN);
        AppSession.getSession().closeAndClear();
        QBSocialLoginCommand.start(context, QBProvider.FACEBOOK, tokenFB, null);
    }

    public void login(String userEmail, String userPassword) {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        QBUser user = new QBUser(null, userPassword, userEmail);
        AppSession.getSession().closeAndClear();
        QBLoginCompositeCommand.start(context, user);
    }

    public void loginChat() {
        QBLoginChatCompositeCommand.start(context);
    }
}