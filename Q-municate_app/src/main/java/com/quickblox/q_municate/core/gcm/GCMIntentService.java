package com.quickblox.q_municate.core.gcm;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.ExistingQbSessionListener;
import com.quickblox.q_municate.ui.authorization.LoginHelper;
import com.quickblox.q_municate.ui.authorization.SplashActivity;
import com.quickblox.q_municate_core.core.gcm.NotificationHelper;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.PrefsHelper;

public class GCMIntentService extends IntentService implements ExistingQbSessionListener {

    public final static int NOTIFICATION_ID = 1;
    public final static long VIBRATOR_DURATION = 1500;

    private String message;
    private String dialogId;
    private int userId;
    private CommandBroadcastReceiver commandBroadcastReceiver;
    private PrefsHelper prefsHelper;
    private LoginHelper loginHelper;

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
        prefsHelper = PrefsHelper.getPrefsHelper();

        if (extras.getString(NotificationHelper.MESSAGE) != null) {
            message = extras.getString(NotificationHelper.MESSAGE);
        }

        if (extras.getString(NotificationHelper.USER_ID) != null) {
            userId = Integer.parseInt(extras.getString(NotificationHelper.USER_ID));
        }

        if (extras.getString(NotificationHelper.DIALOG_ID) != null) {
            dialogId = extras.getString(NotificationHelper.DIALOG_ID);
        }

        boolean chatPush = userId != ConstsCore.ZERO_INT_VALUE && !TextUtils.isEmpty(dialogId);

        if (chatPush) {
            saveOpeningDialogData(userId, dialogId, true);
            commandBroadcastReceiver = new CommandBroadcastReceiver();
            registerCommandBroadcastReceiver();

            loginHelper = new LoginHelper(this, this);
            loginHelper.checkStartExistSession();
        } else {
            saveOpeningDialogData(false);
            sendNotification();
        }
    }

    private void sendNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, SplashActivity.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, ConstsCore.ZERO_INT_VALUE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.ic_launcher).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(message)).setContentText(message).setVibrate(
                new long[]{ConstsCore.ZERO_INT_VALUE, VIBRATOR_DURATION});

        builder.setAutoCancel(true);
        builder.setContentIntent(contentIntent);
        builder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void saveOpeningDialogData(int userId, String dialogId, boolean save) {
        prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_USER_ID, userId);
        prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_DIALOG_ID, dialogId);
        prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_NEED_TO_OPEN_DIALOG, save);
    }

    private void saveOpeningDialogData(boolean save) {
        prefsHelper.savePref(PrefsHelper.PREF_PUSH_MESSAGE_NEED_TO_OPEN_DIALOG, save);
    }

    private void unregisterBroadcastReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(commandBroadcastReceiver);
    }

    private void registerCommandBroadcastReceiver() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(QBServiceConsts.COMMAND_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_FAIL_ACTION);
        intentFilter.addAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);
        intentFilter.addAction(QBServiceConsts.SIGNUP_FAIL_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION);
        intentFilter.addAction(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION);

        LocalBroadcastManager.getInstance(this).registerReceiver(commandBroadcastReceiver, intentFilter);
    }

    @Override
    public void onStartSessionSuccess() {
        loginHelper.loginChat();
    }

    @Override
    public void onStartSessionFail() {
        unregisterBroadcastReceiver();
        sendNotification();
    }

    private class CommandBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            if (intent.getAction().equals(QBServiceConsts.LOGIN_CHAT_COMPOSITE_SUCCESS_ACTION)) {
                saveOpeningDialogData(true);
            } else if (intent.getAction().equals(QBServiceConsts.LOGIN_CHAT_COMPOSITE_FAIL_ACTION)) {
                saveOpeningDialogData(false);
            }
            sendNotification();
        }
    }
}