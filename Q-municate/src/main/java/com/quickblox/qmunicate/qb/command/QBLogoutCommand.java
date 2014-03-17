package com.quickblox.qmunicate.qb.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.facebook.Session;
import com.quickblox.module.auth.QBAuth;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.command.BaseCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class QBLogoutCommand extends BaseCommand {

    private static final String TAG = QBLogoutCommand.class.getSimpleName();

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGOUT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    public QBLogoutCommand(Context context, String resultAction) {
        super(context, resultAction);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        Session.getActiveSession().closeAndClearTokenInformation();
        QBAuth.deleteSession();

        resetFrienList();
        resetRememberMe();
        resetUserCredentials();
        return null;
    }

    private void resetFrienList() {
        App.getInstance().getFriends().clear();
    }

    private void resetRememberMe() {
        App.getInstance().getPrefsHelper().delete(PrefsHelper.PREF_REMEMBER_ME);
    }

    private void resetUserCredentials() {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.delete(PrefsHelper.PREF_USER_EMAIL);
        helper.delete(PrefsHelper.PREF_USER_PASSWORD);
    }
}