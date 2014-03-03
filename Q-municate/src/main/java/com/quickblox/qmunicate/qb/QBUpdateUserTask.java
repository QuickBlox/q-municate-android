package com.quickblox.qmunicate.qb;

import android.app.Activity;

import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.ui.utils.DialogUtils;

public class QBUpdateUserTask extends BaseProgressTask<Object, Void, Void> {

    public QBUpdateUserTask(Activity activity) {
        super(activity);
    }

    @Override
    public Void performInBackground(Object... params) throws Exception {
        QBUser user = (QBUser) params[0];

        String password = user.getPassword();

        user = QBUsers.updateUser(user);
        user.setPassword(password);
        App.getInstance().setUser(user);

        return null;
    }

    @Override
    public void onResult(Void aVoid) {
        super.onResult(aVoid);
        final Activity activity = activityRef.get();
        if (isActivityAlive()) {
            DialogUtils.show(activity, activity.getString(R.string.dlg_user_updated));
        }
    }
}