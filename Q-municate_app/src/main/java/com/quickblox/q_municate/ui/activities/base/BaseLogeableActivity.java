package com.quickblox.q_municate.ui.activities.base;

import android.os.Bundle;

import com.quickblox.q_municate.utils.QBLogeable;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BaseLogeableActivity extends BaseActivity implements QBLogeable {

    protected AtomicBoolean canPerformLogout = new AtomicBoolean(true);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(CAN_PERFORM_LOGOUT)) {
            canPerformLogout = new AtomicBoolean(savedInstanceState.getBoolean(CAN_PERFORM_LOGOUT));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(CAN_PERFORM_LOGOUT, canPerformLogout.get());
        super.onSaveInstanceState(outState);
    }

    //This method is used for logout action when Actvity is going to background
    @Override
    public boolean isCanPerformLogoutInOnStop() {
        return canPerformLogout.get();
    }
}