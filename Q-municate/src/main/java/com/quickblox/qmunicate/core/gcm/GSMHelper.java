package com.quickblox.qmunicate.core.gcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.qb.QBGCMRegistrationTask;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;
import com.quickblox.qmunicate.ui.utils.Utils;

/**
 * Created by vadim on 14.03.14.
 */
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

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
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


    /**
     * Registers the application with GCM servers asynchronously.
     * <p/>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public void registerInBackground() {
        new QBGCMRegistrationTask(activity).execute(gcm);
    }

    public int getSubscriptionId() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        int subscriptionId = prefsHelper.getPref(Consts.SUBSCRIPTION_ID, Consts.NOT_INITIALIZED_VALUE);
        return subscriptionId;
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p/>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     * registration ID.
     */
    public String getRegistrationId() {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        String registrationId = prefsHelper.getPref(Consts.PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int subscriptionId = prefsHelper.getPref(Consts.SUBSCRIPTION_ID, Consts.NOT_INITIALIZED_VALUE);

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefsHelper.getPref(Consts.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersion(activity);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }


    //TODO will be defined throw core qb
    public void subscribeToPushNotifications(String registrationId) {
        new QBGCMRegistrationTask(activity).execute(gcm);
    }
}
