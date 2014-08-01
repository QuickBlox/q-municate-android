package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.core.command.CompositeServiceCommand;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.TipsManager;

public class QBLoginCommand extends CompositeServiceCommand {

    public QBLoginCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        TipsManager.setIsJustLogined(true);
        context.startService(intent);
    }
}