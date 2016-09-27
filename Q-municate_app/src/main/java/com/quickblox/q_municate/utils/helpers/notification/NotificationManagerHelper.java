package com.quickblox.q_municate.utils.helpers.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.authorization.SplashActivity;
import com.quickblox.q_municate.utils.SystemUtils;
import com.quickblox.q_municate_core.models.NotificationEvent;

public class NotificationManagerHelper {

    public final static int NOTIFICATION_ID = NotificationManagerHelper.class.hashCode();

    public static void sendNotificationEvent(Context context, NotificationEvent notificationEvent) {
        Intent intent = SystemUtils.getPreviousIntent(context);
        if (intent == null) {
            intent = new Intent(context, SplashActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
        }
        sendNotificationEvent(context, intent, notificationEvent);
    }

    private static void sendNotificationEvent(Context context, Intent intent, NotificationEvent notificationEvent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(getNotificationIcon())
                .setContentTitle(notificationEvent.getTitle())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationEvent.getSubject()))
                .setContentText(notificationEvent.getBody())
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults = Notification.DEFAULT_ALL;
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private static int getNotificationIcon() {
        boolean whiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        // TODO need to add other icon
        return whiteIcon ? R.drawable.ic_launcher : R.drawable.ic_launcher;
    }

    public static void clearNotificationEvent(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}