package com.quickblox.qmunicate.qb;

import android.app.Activity;
import android.os.Bundle;

import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;

public class QBLoginTask extends BaseProgressTask<Object, Void, Bundle> {

    public static final String PARAM_QBUSER = "qbuser";

    private Callback callback;

    public QBLoginTask(Activity activity) {
        super(activity);
    }

    @Override
    public Bundle performInBackground(Object... params) throws Exception {
        QBUser user = (QBUser) params[0];
        callback = (Callback) params[1];

        QBAuth.createSession();
        user = QBUsers.signIn(user);
        // QBChatService.getInstance().loginWithUser(user);

        App.getInstance().setUser(user);

        Bundle bundle = new Bundle();
        bundle.putSerializable(PARAM_QBUSER, user);

        return bundle;
    }

    @Override
    public void onResult(Bundle bundle) {
        super.onResult(bundle);
        if (isActivityAlive()) {
            callback.onSuccess(bundle);
        }
    }

    public interface Callback {
        void onSuccess(Bundle bundle);
    }
}