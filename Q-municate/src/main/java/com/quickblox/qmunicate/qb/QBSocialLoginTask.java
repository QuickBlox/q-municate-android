package com.quickblox.qmunicate.qb;

import android.support.v4.app.FragmentActivity;

import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.ui.main.MainActivity;

public class QBSocialLoginTask extends BaseProgressTask<String, Void, Void> {

    public QBSocialLoginTask(FragmentActivity activity) {
        super(activity);
    }

    @Override
    public Void performInBackground(String... params) throws Exception {
        String socialProvider = params[0];
        String accessToken = params[1];
        String accessTokenSecret = params[2];

        QBAuth.createSession();
        QBUser user = QBUsers.signInUsingSocialProvider(socialProvider, accessToken, accessTokenSecret);
        // QBChatService.getInstance().loginWithUser(user);
        App.getInstance().setUser(user);

        return null;
    }

    @Override
    public void onResult(Void aVoid) {
        super.onResult(aVoid);
        final FragmentActivity activity = activityRef.get();
        if (isActivityAlive()) {
            MainActivity.startActivity(activity);
            activity.finish();
        }
    }
}