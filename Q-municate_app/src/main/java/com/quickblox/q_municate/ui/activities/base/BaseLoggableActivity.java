package com.quickblox.q_municate.ui.activities.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.quickblox.q_municate.ui.activities.authorization.SplashActivity;
import com.quickblox.q_municate.utils.Loggable;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseLoggableActivity extends BaseActivity implements Loggable {

    public AtomicBoolean canPerformLogout = new AtomicBoolean(true);

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("BaseLoggableActivity", "onRestart()");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        Log.d("BaseLoggableActivity", "onAttachFragment");
        super.onAttachFragment(fragment);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BaseLoggableActivity", "onCreate");

        if (!appInitialized) {
            Log.d("BaseLoggableActivity", "!appInitialized()");
            startSplashActivity();
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(CAN_PERFORM_LOGOUT)) {
            canPerformLogout = new AtomicBoolean(savedInstanceState.getBoolean(CAN_PERFORM_LOGOUT));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CAN_PERFORM_LOGOUT, canPerformLogout.get());
        Log.d("BaseLoggableActivity", "onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    //This method is used for logout action when Activity is going to background
    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return canPerformLogout.get();
    }

    protected void startSplashActivity(){
        SplashActivity.start(this);
        finish();
    }

    protected boolean isCurrentSessionValid() {
        return AppSession.isSessionExistOrNotExpired(TimeUnit.MINUTES.toMillis(
                ConstsCore.TOKEN_VALID_TIME_IN_MINUTES));
    }
}