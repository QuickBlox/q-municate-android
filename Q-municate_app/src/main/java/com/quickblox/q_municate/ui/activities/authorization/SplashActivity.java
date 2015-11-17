package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.listeners.ExistingQbSessionListener;
import com.quickblox.q_municate.utils.helpers.LoginHelper;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseAuthActivity implements ExistingQbSessionListener {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int DELAY_FOR_OPENING_LANDING_ACTIVITY = 1000;

    public static void start(Context context) {
        Intent intent = new Intent(context, SplashActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_splash;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO temp. ONLY FOR TEST WITHOUT TESTERS
        //        Crashlytics.start(this);

        if (isNetworkAvailable()) {
            LoginHelper loginHelper = new LoginHelper(this, this);
            loginHelper.checkStartExistSession();
        } else if (LoginHelper.isCorrectOldAppSession()) {
            startMainActivity();
        } else {
            startLandingActivity();
        }
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

    @Override
    public void checkShowingConnectionError() {
        // nothing. Toolbar is missing.
    }

    private void startLandingActivity() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                LandingActivity.start(SplashActivity.this);
                finish();
            }
        }, DELAY_FOR_OPENING_LANDING_ACTIVITY);
    }
}