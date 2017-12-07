package com.quickblox.q_municate.utils.helpers;


import android.content.Context;
import android.os.PowerManager;

public class PowerManagerHelper {
    private static final int RELEASE_TIME_OUT = 10000;
    private static final String WAKELOCK_TAG = "WakelockTag";

    public static void wakeUpScreen(Context context) {

        PowerManager pm = (PowerManager) context.getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, WAKELOCK_TAG);
            wakeLock.acquire(RELEASE_TIME_OUT);
        }
    }
}
