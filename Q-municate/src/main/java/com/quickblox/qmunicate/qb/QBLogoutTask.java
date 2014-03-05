package com.quickblox.qmunicate.qb;

import android.app.Activity;

import com.facebook.Session;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.ui.login.LoginActivity;

public class QBLogoutTask extends BaseProgressTask<Void, Void, Void> {

    public QBLogoutTask(Activity activity) {
        super(activity);
    }

    @Override
    public Void performInBackground(Void... params) throws Exception {
        Session.getActiveSession().closeAndClearTokenInformation();
        QBAuth.deleteSession();
        return null;
    }

    @Override
    public void onResult(Void aVoid) {
        super.onResult(aVoid);
        final Activity activity = activityRef.get();
        if (isActivityAlive()) {
            LoginActivity.startActivity(activity);
            activity.finish();
        }
    }
}