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
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.core.gcm.NotificationHelper;
import com.quickblox.q_municate.model.PushMessage;
import com.quickblox.q_municate.ui.splash.SplashActivity;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.PrefsHelper;

public class GCMIntentService extends IntentService {

    public final static int NOTIFICATION_ID = 1;
    public final static long VIBRATOR_DURATION = 300;

    private final static String TAG = GCMIntentService.class.getSimpleName();

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
               /* sendNotification("Deleted messages on server: " +
                        extras.toString());*/
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                parseMessage(extras);
            }
        }
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseMessage(Bundle extras) {
        String message = extras.getString(NotificationHelper.MESSAGE, "");
        saveMissedMessageFlag(true);
        sendNotification(message);
    }

    private void saveMissedMessageFlag(boolean isMissedMessage) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_MISSED_MESSAGE, isMissedMessage);
    }

    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, SplashActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, Consts.ZERO_INT_VALUE, intent,
                Consts.ZERO_INT_VALUE);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg).setVibrate(
                new long[]{Consts.ZERO_INT_VALUE, VIBRATOR_DURATION});

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(contentIntent);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
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