package com.quickblox.q_municate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.quickblox.q_municate_core.utils.PrefsHelper;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    private boolean isPushForbidden;

    @Override
    public void onReceive(Context context, Intent intent) {
        isPushForbidden = PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_PUSH_NOTIFICATIONS,
                false);
        if (isPushForbidden) {
            return;
        }
        ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}