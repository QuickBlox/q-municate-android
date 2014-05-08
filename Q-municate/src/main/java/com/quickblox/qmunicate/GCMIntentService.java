package com.quickblox.qmunicate;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.GsonBuilder;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.gcm.NotificationHelper;
import com.quickblox.qmunicate.model.PushMessage;
import com.quickblox.qmunicate.ui.chats.PrivateChatActivity;
import com.quickblox.qmunicate.utils.Consts;


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
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                //sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
               /* sendNotification("Deleted messages on server: " +
                        extras.toString());*/
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                // Post notification of received message.
                parseMessage(extras);
                Log.i(TAG, "Received: " + extras.toString());
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void parseMessage(Bundle extras) {
        String message = extras.getString(NotificationHelper.MESSAGE, "");
        Log.i(TAG, "message=" + message);
        GsonBuilder gsonBuilder = new GsonBuilder();
        PushMessage pushMessage = null;
//        try {
//            pushMessage = gsonBuilder.create().fromJson(message, PushMessage.class);
//        } catch (JsonSyntaxException e) {
//            e.printStackTrace();
//        }
//        if (pushMessage != null && NotificationHelper.CALL_TYPE.equals(pushMessage.getType())) {
            sendNotification(message);
//        }
    }

    private void sendBroadcast(PushMessage message) {
        Intent intent = new Intent();
        intent.setAction(NotificationHelper.ACTION_VIDEO_CALL);
        QBUser qbUser = new QBUser();
        qbUser.setId(message.getUserId());
        intent.putExtra(Consts.USER, qbUser);
        sendBroadcast(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
//        mNotificationManager = (NotificationManager)
//                this.getSystemService(Context.NOTIFICATION_SERVICE);
//
//        Intent contentIntent = new Intent(this, CallActivity.class);
//        contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        contentIntent.putExtra("message", msg);
//
//        int unique_id = (int) System.currentTimeMillis();
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, unique_id,
//                contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_qb)
//                        .setContentTitle(getString(R.string.push_title))
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(msg))
//                        .setContentText(msg)
//                        .setVibrate(new long[]{0, VIBRATOR_DURATION});
//
//        mBuilder.setContentIntent(pendingIntent);
//        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, PrivateChatActivity.class);
        intent.putExtra(NotificationHelper.MESSAGE, msg);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(
                R.drawable.app_icon).setContentTitle(getString(R.string.push_title)).setStyle(
                new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg).setVibrate(
                new long[]{0, VIBRATOR_DURATION});

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
}
