package com.quickblox.qmunicate;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {
    private boolean isPushAllowed;

    public GcmBroadcastReceiver(){
        isPushAllowed = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_PUSH_NOTIFICATIONS, false);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(!isPushAllowed){
            return;
        }
        ComponentName comp = new ComponentName(context.getPackageName(), GCMIntentService.class.getName());
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
