package com.quickblox.qmunicate.qb.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEnvironment;
import com.quickblox.module.messages.model.QBSubscription;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;

// TODO VF move to com.quickblox.qmunicate.core.gcm
public class QBGCMRegistrationTask extends BaseProgressTask<GoogleCloudMessaging, Void, Bundle> {

    private static final String TAG = QBGCMRegistrationTask.class.getSimpleName();
    private Context context;

    public QBGCMRegistrationTask(Activity activity) {
        super(activity, -1);
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
        QBSubscription qbSubscription = subscribeToPushNotifications(registrationId);
        if (qbSubscription != null) {
            registration.putInt(PrefsHelper.PREF_SUBSCRIPTION_ID, qbSubscription.getId());
        }
        return registration;
    }

    private String getRegistrationId(GoogleCloudMessaging gcm) throws IOException {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        String registrationId = prefsHelper.getPref(PrefsHelper.PREF_GCM_SENDER_ID, "");
        if (registrationId.isEmpty()) {
            registrationId = gcm.register(PrefsHelper.PREF_GCM_SENDER_ID);
        }
        return registrationId;
    }

    // TODO VF remove comments

    private QBSubscription subscribeToPushNotifications(String regId) throws QBResponseException {
        Log.d(TAG, "subscribing...");

        String deviceId;

        final TelephonyManager mTelephony = (TelephonyManager) activityRef.get().getSystemService(
                Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId(); //*** use for mobiles
        } else {
            deviceId = Settings.Secure.getString(
                    activityRef.get().getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID); //*** use for tablets
        }
        QBSubscription qbSubscription = null;
        ArrayList<QBSubscription> qbSubscriptions = QBMessages.subscribeToPushNotificationsTask(regId,
                deviceId, QBEnvironment.DEVELOPMENT);
        if (!qbSubscriptions.isEmpty()) {
            qbSubscription = qbSubscriptions.get(0);
        }
        return qbSubscription;
    }

    private void storeRegistration(Context context, Bundle registration) {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        int appVersion = Utils.getAppVersionCode(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        prefsHelper.savePref(PrefsHelper.PREF_REG_ID, registration.getString(PrefsHelper.PREF_REG_ID));
        QBUser user = App.getInstance().getUser();
        if (user != null) {
            prefsHelper.savePref(PrefsHelper.PREF_REG_USER_ID, user.getId());
        }
        prefsHelper.savePref(PrefsHelper.PREF_SUBSCRIPTION_ID, registration.getInt(
                PrefsHelper.PREF_SUBSCRIPTION_ID, Consts.NOT_INITIALIZED_VALUE));
        prefsHelper.savePref(PrefsHelper.PREF_APP_VERSION, appVersion);
    }
}