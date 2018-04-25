package com.quickblox.q_municate.fcm;

import android.os.Bundle;
import android.util.Log;

import com.quickblox.messages.services.fcm.QBFcmPushListenerService;
import com.quickblox.q_municate.utils.helpers.notification.ChatNotificationHelper;

import java.util.Map;

import static com.quickblox.q_municate_core.utils.ConstsCore.*;


public class FcmPushListenerService extends QBFcmPushListenerService {
    private String TAG = FcmPushListenerService.class.getSimpleName();

    private ChatNotificationHelper chatNotificationHelper;

    public FcmPushListenerService() {
        this.chatNotificationHelper = new ChatNotificationHelper(this);
    }

    @Override
    protected void sendPushMessage(Map data, String from, String message) {
        super.sendPushMessage(data, from, message);

        String userId = (String) data.get(MESSAGE_USER_ID);
        String pushMessage = (String) data.get(MESSAGE);
        String dialogId = (String) data.get(MESSAGE_DIALOG_ID);
        String pushMessageType = (String) data.get(MESSAGE_TYPE);
        String pushVOIPType = (String) data.get(MESSAGE_VOIP_TYPE);

        Log.v(TAG, "sendPushMessage\n" + "Message: " + pushMessage + "\nUser ID: " + userId + "\nDialog ID: " + dialogId);

        Bundle extras = new Bundle();
        extras.putString(MESSAGE_USER_ID, userId);
        extras.putString(MESSAGE, pushMessage);
        extras.putString(MESSAGE_DIALOG_ID, dialogId);
        extras.putString(MESSAGE_TYPE, pushMessageType);
        extras.putString(MESSAGE_VOIP_TYPE, pushVOIPType);

        chatNotificationHelper.parseChatMessage(extras);
    }
}
