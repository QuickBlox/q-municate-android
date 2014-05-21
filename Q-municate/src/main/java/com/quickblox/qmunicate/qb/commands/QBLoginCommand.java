package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.CompositeServiceCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBLoginCommand extends CompositeServiceCommand {

    public QBLoginCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        context.startService(intent);
    }
}
