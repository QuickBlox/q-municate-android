package com.quickblox.q_municate.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBEnvironment;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.quickblox.q_municate_core.core.concurrency.BaseErrorAsyncTask;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.io.IOException;
import java.util.ArrayList;

public class QBGCMRegistrationTask extends BaseErrorAsyncTask<GoogleCloudMessaging, Void, Bundle> {

    private static final String TAG = QBGCMRegistrationTask.class.getSimpleName();

    public QBGCMRegistrationTask(Activity activity) {
        super(activity);
    }

    @Override
    public void onResult(Bundle bundle) {
        if (!bundle.isEmpty()) {
            if (bundle.getBoolean(QBServiceConsts.EXTRA_IS_PUSH_SUBSCRIBED_ON_SERVER)) {
                storeRegistration(activityRef.get(), bundle);
            }
        }
    }

    @Override
    public Bundle performInBackground(GoogleCloudMessaging... params) throws Exception {
        GoogleCloudMessaging gcm = params[0];
        Bundle registration = new Bundle();
        String registrationId = getRegistrationId(gcm);
        registration.putString(QBServiceConsts.EXTRA_REGISTRATION_ID, registrationId);
        boolean subscribed = subscribeToPushNotifications(registrationId);
        registration.putBoolean(QBServiceConsts.EXTRA_IS_PUSH_SUBSCRIBED_ON_SERVER, subscribed);
        return registration;
    }

    private String getRegistrationId(GoogleCloudMessaging gcm) throws IOException {
        String registrationId = StringObfuscator.getPushRegistrationAppId();
        registrationId = gcm.register(registrationId);
        return registrationId;
    }

    private boolean subscribeToPushNotifications(String regId) {
        String deviceId = getDeviceIdForMobile(activityRef.get());
        if (deviceId == null) {
            deviceId = getDeviceIdForTablet(activityRef.get());
        }

        ArrayList<QBSubscription> subscriptions = null;
        try {
            subscriptions = QBMessages.subscribeToPushNotificationsTask(regId, deviceId, QBEnvironment.PRODUCTION);
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
        CoreSharedHelper coreSharedHelper = CoreSharedHelper.getInstance();
        coreSharedHelper.savePushRegistrationId(registration.getString(QBServiceConsts.EXTRA_REGISTRATION_ID));
        coreSharedHelper.savePushAppVersion(Utils.getAppVersionCode(context));
    }
}