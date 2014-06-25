package com.quickblox.qmunicate.core.gcm.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class QBGCMRegistrationTask extends BaseProgressTask<GoogleCloudMessaging, Void, Bundle> {

    private static final String TAG = QBGCMRegistrationTask.class.getSimpleName();
    private Context context;

    public QBGCMRegistrationTask(Activity activity) {
        super(activity, Consts.NOT_INITIALIZED_VALUE, false);
    }

    @Override
    public void onResult(Bundle bundle) {
        super.onResult(bundle);
        if (!bundle.isEmpty()) {
            storeRegistration(activityRef.get(), bundle);
        }
    }

    @Override
    public Bundle performInBackground(GoogleCloudMessaging... params) throws Exception {
        GoogleCloudMessaging gcm = params[0];
        Bundle registration = new Bundle();
        String registrationId = getRegistrationId(gcm);
        registration.putString(PrefsHelper.PREF_REG_ID, registrationId);
        boolean subscribed = subscribeToPushNotifications(registrationId);
        if (subscribed) {
            registration.putBoolean(PrefsHelper.PREF_IS_SUBSCRIBED_ON_SERVER, subscribed);
        }
        return registration;
    }

    private String getRegistrationId(GoogleCloudMessaging gcm) throws IOException {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        String registrationId = prefsHelper.getPref(PrefsHelper.PREF_GCM_SENDER_ID, Consts.EMPTY_STRING);
        if (registrationId.isEmpty()) {
            registrationId = gcm.register(PrefsHelper.PREF_GCM_SENDER_ID);
        }
        return registrationId;
    }

    private boolean subscribeToPushNotifications(String regId) {
        String deviceId = getDeviceIdForMobile(activityRef.get());
        if (deviceId == null) {
            deviceId = getDeviceIdForTablet(activityRef.get());
        }

        ArrayList<QBSubscription> subscriptions = null;
        try {
            subscriptions = QBMessages.subscribeToPushNotificationsTask(regId, deviceId,
                    QBEnvironment.DEVELOPMENT);
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        return subscriptions != null;
    }

    private String getDeviceIdForMobile(Context context) {
        final TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(
                Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return null;
        }
        return telephonyManager.getDeviceId();
    }

    private String getDeviceIdForTablet(Context context) {
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID); //*** use for tablets
    }

    private void storeRegistration(Context context, Bundle registration) {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        int appVersion = Utils.getAppVersionCode(context);
        prefsHelper.savePref(PrefsHelper.PREF_REG_ID, registration.getString(PrefsHelper.PREF_REG_ID));
        QBUser user = AppSession.getActiveSession().getUser();
        if (user != null) {
            prefsHelper.savePref(PrefsHelper.PREF_REG_USER_ID, user.getId());
        }

        prefsHelper.savePref(PrefsHelper.PREF_IS_SUBSCRIBED_ON_SERVER, registration.getBoolean(
                PrefsHelper.PREF_IS_SUBSCRIBED_ON_SERVER, false));
        prefsHelper.savePref(PrefsHelper.PREF_APP_VERSION, appVersion);
    }
}