package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;

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
        appInitialized = true;
        AppSession.load();

        processPushIntent();

        if (QBSessionManager.getInstance().getSessionParameters() != null && appSharedHelper.isSavedRememberMe()) {
            startMainActivity();
        } else {
            startLandingActivity();
        }
    }

    private void processPushIntent() {
        boolean openPushDialog = getIntent().getBooleanExtra(QBServiceConsts.EXTRA_SHOULD_OPEN_DIALOG, false);
        CoreSharedHelper.getInstance().saveNeedToOpenDialog(openPushDialog);
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