package com.quickblox.q_municate;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.gcm.NotificationHelper;
import com.quickblox.q_municate_core.models.PushMessage;
import com.quickblox.q_municate.ui.splash.SplashActivity;

public class GCMIntentService extends IntentService {

    public final static int NOTIFICATION_ID = 1;
    public final static long VIBRATOR_DURATION = 1500;

    private NotificationManager notificationManager;
    private String message;
    private String dialogId;
    private int userId;

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
        if (extras.getString(NotificationHelper.MESSAGE) != null) {
            message = extras.getString(NotificationHelper.MESSAGE);
        }

        if (extras.getString(NotificationHelper.USER_ID) != null) {
            userId = Integer.parseInt(extras.getString(NotificationHelper.USER_ID));
        }

        if (extras.getString(NotificationHelper.DIALOG_ID) != null) {
            dialogId = extras.getString(NotificationHelper.DIALOG_ID);
        }

        sendNotification();
    }

    private void sendNotification() {
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, SplashActivity.class);

        saveOpeningDialogData(userId, dialogId);

        PendingIntent contentIntent = PendingIntent.getActivity(this, ConstsCore.ZERO_INT_VALUE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message).setVibrate(
                new long[]{ConstsCore.ZERO_INT_VALUE, VIBRATOR_DURATION});

        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void saveOpeningDialogData(int userId, String dialogId) {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        if (userId != ConstsCore.ZERO_INT_VALUE && !TextUtils.isEmpty(dialogId)) {
            prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_USER_ID, userId);
            prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_DIALOG_ID, dialogId);
            prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_NEED_TO_OPEN_DIALOG, true);
        } else {
            prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_NEED_TO_OPEN_DIALOG, false);
        }
    }

    private void sendBroadcast(PushMessage message) {
        Intent intent = new Intent();
        intent.setAction(NotificationHelper.ACTION_VIDEO_CALL);
        QBUser qbUser = new QBUser();
        qbUser.setId(message.getUserId());
        intent.putExtra(ConstsCore.USER, qbUser);
        sendBroadcast(intent);
    }
}