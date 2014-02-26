package com.quickblox.qmunicate.ui.splash;

import android.content.SharedPreferences;
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
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.wellcome.WellcomeActivity;

public class SplashActivity extends FacebookActivity implements QBLoginTask.Callback {

    private static final String TAG = FacebookActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        String userEmail = getUserEmail();
        String userPassword = getUserPassword();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);

        if (Session.getActiveSession().isOpened()) {
            return;
        }

        if (isRememberMe() && isEmailEntered && isPasswordEntered) {
            login(userEmail, userPassword);
        } else if (isWellcomeShown()) {
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

    private String getUserEmail() {
        SharedPreferences prefs = App.getInstance().getSharedPreferences();
        return prefs.getString(Consts.PREF_USER_EMAIL, null);
    }

    private String getUserPassword() {
        SharedPreferences prefs = App.getInstance().getSharedPreferences();
        return prefs.getString(Consts.PREF_USER_PASSWORD, null);
    }

    private boolean isWellcomeShown() {
        SharedPreferences prefs = App.getInstance().getSharedPreferences();
        return prefs.getBoolean(Consts.PREF_WELLCOME_SHOWN, false);
    }
}