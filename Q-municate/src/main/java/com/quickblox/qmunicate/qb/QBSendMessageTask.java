package com.quickblox.qmunicate.qb;

import android.app.Activity;
import android.content.Context;

import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.core.gcm.NotificationHelper;

// TODO VF remake as Command
public class QBSendMessageTask extends BaseProgressTask<Object, Void, QBEvent> {

    private static final String TAG = QBGCMRegistrationTask.class.getSimpleName();
    private Context context;

    public QBSendMessageTask(Activity activity) {
        super(activity, -1);
    }

    @Override
    public QBEvent performInBackground(Object... params) throws Exception {
        QBUser qbUser = (QBUser) params[0];
        String msg = (String) params[1];
        String type = (String) params[2];
        QBEvent pushEvent = NotificationHelper.createPushEvent(qbUser, msg, type);
        QBEvent event = QBMessages.createEvent(pushEvent);
        return event;
    }
}
