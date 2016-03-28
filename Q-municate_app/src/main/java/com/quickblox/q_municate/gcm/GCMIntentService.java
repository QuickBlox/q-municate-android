package com.quickblox.q_municate.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.q_municate.utils.helpers.notification.ChatNotificationHelper;

public class GCMIntentService extends IntentService {

    private static String TAG = GCMIntentService.class.getSimpleName();

    public GCMIntentService() {
        super(GCMIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(this);
        String messageType = googleCloudMessaging.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                new ChatNotificationHelper(this).parseChatMessage(extras);
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }
}