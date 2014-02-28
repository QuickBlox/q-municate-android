package com.quickblox.qmunicate.ui.splash;

import android.os.Bundle;
import android.text.TextUtils;

import com.facebook.Session;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLoginTask;
import com.quickblox.qmunicate.ui.base.FacebookActivity;
import com.quickblox.qmunicate.ui.login.LoginActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;
import com.quickblox.qmunicate.ui.wellcome.WellcomeActivity;

public class SplashActivity extends FacebookActivity implements QBLoginTask.Callback {

    private static final String TAG = FacebookActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String userEmail = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_USER_EMAIL);
        String userPassword = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_USER_PASSWORD);

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);
        boolean isRememberMe = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, false);

        if (Session.getActiveSession().isOpened()) {
            return;
        }

        boolean isWellcomeShown = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_WELLCOME_SHOWN, false);

        if (isRememberMe && isEmailEntered && isPasswordEntered) {
            login(userEmail, userPassword);
        } else if (isWellcomeShown) {
            LoginActivity.startActivity(SplashActivity.this);
            finish();
        } else {
            WellcomeActivity.startAtivity(SplashActivity.this);
            finish();
        }
    }

    @Override
    public void onSuccess(Bundle bundle) {
        MainActivity.startActivity(SplashActivity.this);
        finish();
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        new QBLoginTask(this).execute(user, this);
    }
}