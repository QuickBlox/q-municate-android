package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;

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
    public Bundle perform(Bundle extras) throws QBResponseException {
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
        ChatDatabaseManager.clearAllCache(context);
    }

    private void resetRememberMe() {
        PrefsHelper.getPrefsHelper().delete(PrefsHelper.PREF_REMEMBER_ME);
    }

    private void resetUserData() {
        PrefsHelper helper = PrefsHelper.getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_USER_PASSWORD);
        helper.delete(PrefsHelper.PREF_STATUS);
    }
}