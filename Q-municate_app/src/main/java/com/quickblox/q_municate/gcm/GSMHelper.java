package com.quickblox.q_municate.gcm;

import android.app.Activity;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate.tasks.QBGCMRegistrationTask;

public class GSMHelper {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String TAG = GSMHelper.class.getSimpleName();

    private Activity activity;
    private GoogleCloudMessaging googleCloudMessaging;
    private CoreSharedHelper coreSharedHelper;

    public GSMHelper(Activity activity) {
        this.activity = activity;
        googleCloudMessaging = GoogleCloudMessaging.getInstance(activity);
        coreSharedHelper = CoreSharedHelper.getInstance();
    }

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    public void registerInBackground() {
        new QBGCMRegistrationTask(activity).execute(googleCloudMessaging);
    }

    public boolean isDeviceRegisteredWithUser() {
        String registrationId = coreSharedHelper.getPushRegistrationId();
        int registeredVersion = coreSharedHelper.getPushAppVersion();
        int currentVersion = Utils.getAppVersionCode(activity);
        boolean currAppVersionRegistered = registeredVersion == currentVersion;

        return !TextUtils.isEmpty(registrationId) && currAppVersionRegistered;
    }
}