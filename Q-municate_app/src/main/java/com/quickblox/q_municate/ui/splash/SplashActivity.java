package com.quickblox.q_municate.ui.splash;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.authorization.landing.LandingActivity;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.main.MainActivity;
import com.quickblox.q_municate.utils.AnalyticsUtils;
import com.quickblox.q_municate.utils.FacebookHelper;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoginCommand;
import com.quickblox.q_municate_core.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

import java.util.concurrent.TimeUnit;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private FacebookHelper facebookHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        addActions();

        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        String userEmail = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_USER_EMAIL);
        String userPassword = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_USER_PASSWORD);

        boolean isRememberMe = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, false);

        if (isRememberMe) {
            checkStartExistSession(userEmail, userPassword);
        } else {
            startLanding();
        }

        setContentView(R.layout.activity_splash);
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggedInToChat()) {
            startMainActivity();
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    protected void onFailAction(String action) {
        super.onFailAction(action);
        startLanding();
    }

    private boolean isLoggedInToChat() {
        return QBChatService.isInitialized() && QBChatService.getInstance().isLoggedIn();
    }

    private void checkStartExistSession(String userEmail, String userPassword) {
        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);
        if ((isEmailEntered && isPasswordEntered) || (isLoggedViaFB(isPasswordEntered))) {
            runExistSession(userEmail, userPassword);
        } else {
            startLanding();
        }
    }

    private boolean isLoggedViaFB(boolean isPasswordEntered) {
        return isPasswordEntered && LoginType.FACEBOOK.equals(getCurrentLoginType());
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, new LoginSuccessAction());
        addAction(QBServiceConsts.LOGIN_AND_JOIN_CHATS_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);
    }

    public boolean isLoggedViaFB() {
        return facebookHelper.isSessionOpened() && LoginType.FACEBOOK.equals(getCurrentLoginType());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void startLanding() {
        LandingActivity.start(SplashActivity.this);
        finish();
    }

    private void runExistSession(String userEmail, String userPassword) {
        //check is token valid for about 1 minute
        if (AppSession.isSessionExistOrNotExpired(TimeUnit.MINUTES.toMillis(
                ConstsCore.TOKEN_VALID_TIME_IN_MINUTES))) {
            startMainActivity();
            finish();
        } else {
            doAutoLogin(userEmail, userPassword);
        }
    }

    private void doAutoLogin(String userEmail, String userPassword) {
        if (LoginType.EMAIL.equals(getCurrentLoginType())) {
            login(userEmail, userPassword);
        } else {
            FacebookHelper.logout();
            facebookHelper.loginWithFacebook();
        }
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        QBLoginCommand.start(this, user);
    }

    private LoginType getCurrentLoginType() {
        return AppSession.getSession().getLoginType();
    }

    private void startMainActivity() {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        MainActivity.start(SplashActivity.this);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened() && LoginType.FACEBOOK.equals(getCurrentLoginType())) {
                QBLoginRestWithSocialCommand.start(SplashActivity.this, QBProvider.FACEBOOK,
                        session.getAccessToken(), null);
            }
        }
    }

    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);

            startMainActivity();

            AnalyticsUtils.pushAnalyticsData(SplashActivity.this, user, "User Sign In");

            finish();
        }
    }
}