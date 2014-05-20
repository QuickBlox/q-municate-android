package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class QBLogoutCommand extends ServiceCommand {

    private static final String TAG = QBLogoutCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBLogoutCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGOUT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        authHelper.logout();
        resetFriendList();
        resetRememberMe();
        resetUserData();
        return null;
    }

    private void resetFriendList() {
        DatabaseManager.deleteAllFriends(context);
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