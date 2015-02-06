package com.quickblox.q_municate.ui.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.quickblox.q_municate.R;

public class SplashActivity extends BaseAuthActivity implements LoginHelper.ExistingSessionListener {

    private static final String TAG = SplashActivity.class.getSimpleName();

    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.activity_splash);

        LoginHelper loginHelper = new LoginHelper(SplashActivity.this, this, checkedRememberMe);

        loginHelper.checkStartExistSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggedInToServer()) {
            startMainActivity();
            finish();
        }
    }

    private void startLanding() {
        LandingActivity.start(SplashActivity.this);
    }

    @Override
    public void onStartSessionSuccess() {
        checkedRememberMe = true;
        startMainActivity();
        finish();
    }

    @Override
    public void onStartSessionFail() {
        startLanding();
        finish();
    }
}