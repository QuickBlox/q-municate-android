package com.quickblox.q_municate_core.legacy.qb.commands.rest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.legacy.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.legacy.service.QBService;
import com.quickblox.q_municate_core.legacy.service.QBServiceConsts;
import com.quickblox.q_municate_core.legacy.utils.helpers.CoreSharedHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.utils.ErrorUtils;

public class QBLogoutCompositeCommand extends CompositeServiceCommand {

    private static final String TAG = QBLogoutCompositeCommand.class.getSimpleName();

    public QBLogoutCompositeCommand(Context context, String successAction, String failAction) {
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
            resetSharedPreferences();
        } catch (Exception e) {
            ErrorUtils.logError(TAG, e);
        }
        return extras;
    }

    private void resetCacheData() {
        DataManager.getInstance().clearAllTables();
    }

    private void resetSharedPreferences() {
        CoreSharedHelper.getInstance().clearAll();
    }
}