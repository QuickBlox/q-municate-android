package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;

import com.quickblox.qmunicate.core.command.CompositeServiceCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBLoginAndJoinDialogsCommand extends CompositeServiceCommand {

    public QBLoginAndJoinDialogsCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_AND_JOIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }
}
