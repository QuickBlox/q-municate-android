package com.quickblox.qmunicate.qb;

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
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.concurrency.BaseProgressTask;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;
import com.quickblox.qmunicate.ui.utils.Utils;

import java.util.ArrayList;

/**
 * Created by vadim on 14.03.14.
 */
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
        String regid = gcm.register(Consts.SENDER_ID);
        registration.putString(Consts.PROPERTY_REG_ID, regid);
        //msg = "Device registered, registration ID=" + regid;

        // You should send the registration ID to your server over HTTP, so it
        // can use GCM/HTTP or CCS to send messages to your app.
        QBSubscription qbSubscription = subscribeToPushNotifications(regid);
        if (qbSubscription != null) {
            registration.putInt(Consts.SUBSCRIPTION_ID, qbSubscription.getId());
        }
        // For this demo: we don't need to send it because the device will send
        // upstream messages to a server that echo back the message using the
        // 'from' address in the message.
        return registration;
    }


    /**
     * Subscribe to Push Notifications
     *
     * @param regId registration ID
     */
    private QBSubscription subscribeToPushNotifications(String regId) throws QBResponseException {
        //Create push token with  Registration Id for Android
        //
        Log.d(TAG, "subscribing...");

        String deviceId;

        final TelephonyManager mTelephony = (TelephonyManager) activityRef.get().getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephony.getDeviceId() != null) {
            deviceId = mTelephony.getDeviceId(); //*** use for mobiles
        } else {
            deviceId = Settings.Secure.getString(activityRef.get().getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID); //*** use for tablets
        }
        QBSubscription qbSubscription = null;
        ArrayList<QBSubscription> qbSubscriptions = QBMessages.subscribeToPushNotificationsTask(regId, deviceId, QBEnvironment.DEVELOPMENT);
        if (!qbSubscriptions.isEmpty()) {
            qbSubscription = qbSubscriptions.get(0);
        }
        return qbSubscription;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context      application's context.
     * @param registration registration bundle
     */
    private void storeRegistration(Context context, Bundle registration) {
        PrefsHelper prefsHelper = App.getInstance().getPrefsHelper();
        int appVersion = Utils.getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        prefsHelper.savePref(Consts.PROPERTY_REG_ID, registration.getString(Consts.PROPERTY_REG_ID));
        prefsHelper.savePref(Consts.PROPERTY_REG_ID, registration.getInt(Consts.SUBSCRIPTION_ID, Consts.NOT_INITIALIZED_VALUE));
        prefsHelper.savePref(Consts.PROPERTY_APP_VERSION, appVersion);
    }

}
