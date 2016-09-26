package com.quickblox.q_municate_core.legacy.qb.commands.chat;

import android.content.Context;
import android.content.Intent;

import com.quickblox.q_municate_core.legacy.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.legacy.service.QBService;
import com.quickblox.q_municate_core.legacy.service.QBServiceConsts;

public class QBLoginChatCompositeCommand extends CompositeServiceCommand {

    public QBLoginChatCompositeCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_COMPOSITE_ACTION, null, context, QBService.class);
        context.startService(intent);
    }
}