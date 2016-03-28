package com.quickblox.q_municate.gcm;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.quickblox.q_municate.App;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private static String TAG = GcmBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enablePush = App.getInstance().getAppSharedHelper().isEnablePushNotifications();
        Log.d(TAG, "--- PUSH. onReceive(), show notification = " + enablePush + " ---");
        if (!enablePush) {
            return;
        }
        ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}