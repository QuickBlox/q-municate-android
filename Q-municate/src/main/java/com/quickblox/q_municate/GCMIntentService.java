package com.quickblox.q_municate;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.core.gcm.NotificationHelper;
import com.quickblox.q_municate.model.PushMessage;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.splash.SplashActivity;
import com.quickblox.q_municate.utils.Consts;

public class GCMIntentService extends IntentService {

    public final static int NOTIFICATION_ID = 1;
    public final static long VIBRATOR_DURATION = 1500;

    private NotificationManager notificationManager;
    private String message;
    private String dialogId;
    private String userId;

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                parseMessage(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseMessage(Bundle extras) {
        message = extras.getString(NotificationHelper.MESSAGE);
        dialogId = extras.getString(NotificationHelper.DIALOG_ID);
        userId = extras.getString(NotificationHelper.USER_ID);

        sendNotification();
    }

    private void sendNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, SplashActivity.class);

        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, Consts.ZERO_INT_VALUE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message).setVibrate(
                new long[]{Consts.ZERO_INT_VALUE, VIBRATOR_DURATION});

        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void sendBroadcast(PushMessage message) {
        Intent intent = new Intent();
        intent.setAction(NotificationHelper.ACTION_VIDEO_CALL);
        QBUser qbUser = new QBUser();
        qbUser.setId(message.getUserId());
        intent.putExtra(Consts.USER, qbUser);
        sendBroadcast(intent);
    }
}