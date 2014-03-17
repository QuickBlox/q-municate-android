package com.quickblox.qmunicate.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.receiver.BaseBroadcastReceiver;
import com.quickblox.qmunicate.qb.command.QBLoginCommand;
import com.quickblox.qmunicate.qb.command.QBSocialLoginCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.landing.LandingActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class SplashActivity extends BaseActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private FacebookHelper facebookHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        registerReceiver(new LoginBroadcastReceiver(), QBServiceConsts.LOGIN_RESULT);
        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        if (facebookHelper.isSessionOpened()) {
            return;
        }

        String userEmail = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_USER_EMAIL);
        String userPassword = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_USER_PASSWORD);

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);
        boolean isRememberMe = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, false);

        boolean isWellcomeShown = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_LANDING_SHOWN, false);

        if (isRememberMe && isEmailEntered && isPasswordEntered) {
            login(userEmail, userPassword);
        } else if (isWellcomeShown) {
            LoginActivity.start(SplashActivity.this);
            finish();
        } else {
            LandingActivity.start(SplashActivity.this);
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookHelper.onSaveInstanceState(outState);
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        QBLoginCommand.start(this, user);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                QBSocialLoginCommand.start(SplashActivity.this, QBProvider.FACEBOOK, session.getAccessToken(), null);
            }
        }
    }

    private class LoginBroadcastReceiver extends BaseBroadcastReceiver {

        @Override
        public void onResult(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().setUser(user);
            MainActivity.start(SplashActivity.this);
            finish();
        }
    }
}