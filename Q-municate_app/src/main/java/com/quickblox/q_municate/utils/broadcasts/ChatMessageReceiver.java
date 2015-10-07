package com.quickblox.q_municate.utils.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.quickblox.q_municate.utils.SystemUtils;
import com.quickblox.q_municate.utils.helpers.notification.ChatNotificationHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.User;

public class ChatMessageReceiver extends BroadcastReceiver {

    private static final String TAG = ChatMessageReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "--- onReceive() ---");

        if (!SystemUtils.isAppRunningNow()) {
            ChatNotificationHelper chatNotificationHelper = new ChatNotificationHelper(context);

            String message = intent.getStringExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE);
            User user = (User) intent.getSerializableExtra(QBServiceConsts.EXTRA_USER);
            String dialogId = intent.getStringExtra(QBServiceConsts.EXTRA_DIALOG_ID);

            chatNotificationHelper.saveOpeningDialogData(true, user.getUserId(), dialogId);
            chatNotificationHelper.saveOpeningDialogData(true);
            chatNotificationHelper.sendNotification(message);
        }
    }
}