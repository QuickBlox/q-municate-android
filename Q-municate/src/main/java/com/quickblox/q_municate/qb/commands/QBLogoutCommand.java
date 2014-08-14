package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.caching.DatabaseManager;
import com.quickblox.q_municate.core.command.CompositeServiceCommand;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.PrefsHelper;

public class QBLogoutCommand extends CompositeServiceCommand {

    private static final String TAG = QBLogoutCommand.class.getSimpleName();

    public QBLogoutCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGOUT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        try {
            super.perform(extras);
            resetCacheData();
            resetRememberMe();
            resetUserData();
        } catch (Exception e) {
            ErrorUtils.logError(TAG, e);
        }
        return extras;
    }

    private void resetCacheData() {
        DatabaseManager.clearAllCache(context);
    }

    private void resetRememberMe() {
        App.getInstance().getPrefsHelper().delete(PrefsHelper.PREF_REMEMBER_ME);
    }

    private void resetUserData() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_USER_PASSWORD);
        helper.delete(PrefsHelper.PREF_STATUS);
    }
}