package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBLoginRestCommand extends ServiceCommand {

    private static final String TAG = QBLoginRestCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBLoginRestCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_REST_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        Log.d(TAG, "--- perform() ---");
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        user = authHelper.login(user);
        extras.putSerializable(QBServiceConsts.EXTRA_USER, user);
        return extras;
    }
}
