package com.quickblox.q_municate.core.gcm;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.messages.QBMessages;
import com.quickblox.messages.model.QBSubscription;
import com.quickblox.q_municate.core.concurrency.BaseProgressTask;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

/**
 * Created on 6/12/15.
 *
 * @author Bogatov Evgeniy bogatovevgeniy@gmail.com
 */

public class QBGCMUnregistrationTask extends BaseProgressTask<GoogleCloudMessaging, Void, Bundle> {

    private static final String TAG = QBGCMUnregistrationTask.class.getSimpleName();

    public QBGCMUnregistrationTask(Activity activity) {
        super(activity, ConstsCore.NOT_INITIALIZED_VALUE, false);
    }

    @Override
    public void onResult(Bundle bundle) {
        super.onResult(bundle);
//        if (!bundle.isEmpty()) {
//            storeRegistration(activityRef.get(), bundle);
//        }
    }

    @Override
    public Bundle performInBackground(GoogleCloudMessaging... params) throws Exception {
        GoogleCloudMessaging gcm = params[0];
        Bundle registration = new Bundle();
        gcm.unregister();
        registration.putString(PrefsHelper.PREF_REG_ID, ConstsCore.EMPTY_STRING);
//        boolean subscribed = unSubscribeToPushNotifications(registrationId);
//        registration.putBoolean(PrefsHelper.PREF_IS_SUBSCRIBED_ON_SERVER, subscribed);
        return registration;
    }

    private boolean unSubscribeToPushNotifications(String regId) {
        ArrayList<QBSubscription> subscriptions = null;
        try {
            subscriptions = QBMessages.getSubscriptions();

            // Get device id for subscription removing
            String deviceId = getDeviceIdForMobile(activityRef.get());
            if (deviceId == null) {
                deviceId = getDeviceIdForTablet(activityRef.get());
            }


            for (QBSubscription subscription : subscriptions) {
                if (subscription.getDevice().getId().equals(deviceId)) {
                    QBMessages.deleteSubscription(subscription.getId());
                    break;
                }
            }
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
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        int appVersion = Utils.getAppVersionCode(context);
        prefsHelper.savePref(PrefsHelper.PREF_REG_ID, registration.getString(PrefsHelper.PREF_REG_ID));
        QBUser user = AppSession.getSession().getUser();
        if (user != null) {
            prefsHelper.savePref(PrefsHelper.PREF_REG_USER_ID, user.getId());
        }

        prefsHelper.savePref(PrefsHelper.PREF_IS_SUBSCRIBED_ON_SERVER, registration.getBoolean(
                PrefsHelper.PREF_IS_SUBSCRIBED_ON_SERVER, false));
        prefsHelper.savePref(PrefsHelper.PREF_APP_VERSION, appVersion);
    }
}
