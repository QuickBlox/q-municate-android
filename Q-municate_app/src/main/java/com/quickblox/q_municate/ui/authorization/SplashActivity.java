package com.quickblox.q_municate.ui.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.ExistingQbSessionListener;

public class SplashActivity extends BaseAuthActivity implements ExistingQbSessionListener {

    private static final String TAG = SplashActivity.class.getSimpleName();

    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO temp. ONLY FOR TEST WITHOUT TESTERS
        //        Crashlytics.start(this);

        setContentView(R.layout.activity_splash);

        activateButterKnife();

        LoginHelper loginHelper = new LoginHelper(this, this);
        loginHelper.checkStartExistSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isLoggedInToServer()) {
            startMainActivity(true);
        }
    }

    @Override
    public void onStartSessionSuccess() {
        appSharedHelper.saveSavedRememberMe(true);
        startMainActivity(true);
    }

    @Override
    public void onStartSessionFail() {
        startLandingActivity();
    }

    private void startLandingActivity() {
        LandingActivity.start(this);
        finish();
    }
}