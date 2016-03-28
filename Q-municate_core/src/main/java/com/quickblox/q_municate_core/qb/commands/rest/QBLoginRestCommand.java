package com.quickblox.q_municate_core.qb.commands.rest;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

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
        QBUser qbUser = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        qbUser = authHelper.login(qbUser);
        extras.putSerializable(QBServiceConsts.EXTRA_USER, qbUser);
        return extras;
    }
}