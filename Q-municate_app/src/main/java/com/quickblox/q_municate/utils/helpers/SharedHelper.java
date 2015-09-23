package com.quickblox.q_municate.utils.helpers;

import android.content.Context;

import com.quickblox.q_municate_core.utils.CoreSharedHelper;

public class SharedHelper extends CoreSharedHelper {

    public interface Constants {

        String USER_AGREEMENT = "user_agreement";
        String REMEMBER_ME = "remember_me";
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
}