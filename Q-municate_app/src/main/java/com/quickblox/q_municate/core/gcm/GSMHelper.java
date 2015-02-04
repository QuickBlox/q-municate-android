package com.quickblox.q_municate.core.gcm;

import android.app.Activity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.core.gcm.tasks.QBGCMRegistrationTask;
import com.quickblox.q_municate_core.utils.PrefsHelper;

public class GSMHelper {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String TAG = GSMHelper.class.getSimpleName();
    private GoogleCloudMessaging gcm;
    private Activity activity;

    public GSMHelper(Activity activity) {
        this.activity = activity;
        gcm = GoogleCloudMessaging.getInstance(activity);
    }

    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            }
            return false;
        }
        return true;
    }

    public void registerInBackground() {
        new QBGCMRegistrationTask(activity).execute(gcm);
    }

    public boolean isDeviceRegisteredWithUser(QBUser user) {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        String registrationId = prefsHelper.getPref(PrefsHelper.PREF_REG_ID, ConstsCore.EMPTY_STRING);

        boolean userSavedInPreference = isUserSavedInPreference(prefsHelper, user);

        int registeredVersion = prefsHelper.getPref(PrefsHelper.PREF_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersionCode(activity);
        boolean currAppVersionRegistered = registeredVersion == currentVersion;
        return (!registrationId.isEmpty()) && (userSavedInPreference) &&
                (currAppVersionRegistered);
    }

    private boolean isUserSavedInPreference(PrefsHelper prefsHelper, QBUser user) {
        int registeredUserId = prefsHelper.getPref(PrefsHelper.PREF_REG_USER_ID,
                ConstsCore.NOT_INITIALIZED_VALUE);
        return user.getId() == registeredUserId;
    }
}
