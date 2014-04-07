package com.quickblox.qmunicate.core.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.qb.tasks.QBGCMRegistrationTask;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.Utils;

public class GSMHelper {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String TAG = GSMHelper.class.getSimpleName();
    private GoogleCloudMessaging gcm;
    private SharedPreferences prefs;
    private Context context;
    private String regid;
    private Activity activity;

    public GSMHelper(Activity activity) {
        this.activity = activity;
        gcm = GoogleCloudMessaging.getInstance(activity);
    }

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    public void registerInBackground() {
        new QBGCMRegistrationTask(activity).execute(gcm);
    }

    public int getSubscriptionId() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        int subscriptionId = prefsHelper.getPref(Consts.SUBSCRIPTION_ID, Consts.NOT_INITIALIZED_VALUE);
        return subscriptionId;
    }

    public String getRegistrationId() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        String registrationId = prefsHelper.getPref(Consts.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int subscriptionId = prefsHelper.getPref(Consts.SUBSCRIPTION_ID, Consts.NOT_INITIALIZED_VALUE);

        int registeredVersion = prefsHelper.getPref(Consts.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersionCode(activity);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    //TODO VF will be defined throw core qb
    public void subscribeToPushNotifications(String registrationId) {
        new QBGCMRegistrationTask(activity).execute(gcm);
    }
}
