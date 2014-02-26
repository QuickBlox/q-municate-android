package com.quickblox.qmunicate.qb;

import android.support.v4.app.FragmentActivity;

import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.ui.main.MainActivity;

public class QBUpdateUserTask extends BaseProgressTask<Object, Void, Void> {

    public QBUpdateUserTask(FragmentActivity activity) {
        super(activity);
    }

    @Override
    public Void performInBackground(Object... params) throws Exception {
        QBUser user = (QBUser) params[0];

        user = QBUsers.updateUser(user);
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