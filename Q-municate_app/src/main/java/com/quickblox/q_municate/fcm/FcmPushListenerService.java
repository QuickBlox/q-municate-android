package com.quickblox.q_municate.fcm;

import android.os.Bundle;

import com.google.firebase.messaging.RemoteMessage;
import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.q_municate.utils.helpers.notification.ChatNotificationHelper;

import java.util.Map;


public class FcmPushListenerService extends QBFcmPushListenerService {
    private String TAG = FcmPushListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);

        Bundle extras = new Bundle();
        extras.putString(ChatNotificationHelper.MESSAGE, message);
        extras.putString(ChatNotificationHelper.USER_ID, (String) data.get(ChatNotificationHelper.USER_ID));
        extras.putString(ChatNotificationHelper.DIALOG_ID, (String) data.get(ChatNotificationHelper.DIALOG_ID));

        new ChatNotificationHelper(this).parseChatMessage(extras);
    }
}
