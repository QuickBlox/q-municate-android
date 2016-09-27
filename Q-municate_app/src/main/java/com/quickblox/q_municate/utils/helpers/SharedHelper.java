package com.quickblox.q_municate.utils.helpers;

import android.content.Context;

import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;

public class SharedHelper extends CoreSharedHelper {

    public class Constants {

        public static final String USER_AGREEMENT = "user_agreement";
        public static final String REMEMBER_ME = "remember_me";
        public static final String ENABLING_PUSH_NOTIFICATIONS = "enabling_push_notifications";
    }

    public SharedHelper(Context context) {
        super(context);
    }

    public boolean isShownUserAgreement() {
        return getPref(Constants.USER_AGREEMENT, false);
    }

    public void saveShownUserAgreement(boolean save) {
        savePref(Constants.USER_AGREEMENT, save);
    }

    public boolean isSavedRememberMe() {
        return getPref(Constants.REMEMBER_ME, false);
    }

    public void saveSavedRememberMe(boolean save) {
        savePref(Constants.REMEMBER_ME, save);
    }

    public boolean isEnablePushNotifications() {
        return getPref(Constants.ENABLING_PUSH_NOTIFICATIONS, true);
    }

    public void saveEnablePushNotifications(boolean enable) {
        savePref(Constants.ENABLING_PUSH_NOTIFICATIONS, enable);
    }
}