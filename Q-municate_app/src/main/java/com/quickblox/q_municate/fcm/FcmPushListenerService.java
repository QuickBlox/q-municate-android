package com.quickblox.q_municate.fcm;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.q_municate.utils.helpers.notification.ChatNotificationHelper;

import java.util.Map;


public class FcmPushListenerService extends QBFcmPushListenerService {
    private String TAG = FcmPushListenerService.class.getSimpleName();

    private ChatNotificationHelper chatNotificationHelper;

    public FcmPushListenerService() {
        this.chatNotificationHelper = new ChatNotificationHelper(this);
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);

        String userId = (String) data.get(ChatNotificationHelper.USER_ID);
        String pushMessage = (String) data.get(ChatNotificationHelper.MESSAGE);
        String dialogId = (String) data.get(ChatNotificationHelper.DIALOG_ID);

        Log.v(TAG, "sendPushMessage\n" + "Message: " + pushMessage + "\nUser ID: " + userId + "\nDialog ID: " + dialogId);

        Bundle extras = new Bundle();
        extras.putString(ChatNotificationHelper.USER_ID, userId);
        extras.putString(ChatNotificationHelper.MESSAGE, pushMessage);
        extras.putString(ChatNotificationHelper.DIALOG_ID, dialogId);

        chatNotificationHelper.parseChatMessage(extras);
    }
}
