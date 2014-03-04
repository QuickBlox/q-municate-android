package com.quickblox.qmunicate.qb;

import android.app.Activity;

import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.utils.Consts;

public class QBRemoveFriendTask extends BaseProgressTask<Object, Void, Void> {
    private Callback callback;

    public QBRemoveFriendTask(Activity activity) {
        super(activity);
    }

    @Override
    public Void performInBackground(Object... params) throws Exception {
        Friend friend = (Friend) params[0];
        callback = (Callback) params[1];

        QBCustomObject newObject = new QBCustomObject(Consts.FRIEND_CLASS_NAME);
        newObject.put(Consts.FRIEND_FIELD_FRIEND_ID, friend.getId());

        QBCustomObjects.createObject(newObject);

        return null;
    }

    @Override
    public void onResult(Void aVoid) {
        super.onResult(aVoid);
        if (isActivityAlive()) {
            callback.onSuccess();
        }
    }

    public interface Callback {
        void onSuccess();
    }
}