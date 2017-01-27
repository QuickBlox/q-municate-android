package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.listeners.ExistingQbSessionListener;
import com.quickblox.q_municate.utils.helpers.LoginHelper;
import com.quickblox.q_municate_core.models.AppSession;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends BaseAuthActivity  {

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
        Log.d(TAG, "onCreate");

        AppSession.load();

        if (QBSessionManager.getInstance().getSessionParameters() != null && appSharedHelper.isSavedRememberMe()) {
            startMainActivity();
        } else {
            startLandingActivity();
        }
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