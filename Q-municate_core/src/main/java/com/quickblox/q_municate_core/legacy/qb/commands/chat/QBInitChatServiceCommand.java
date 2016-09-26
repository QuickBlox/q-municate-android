package com.quickblox.q_municate_core.legacy.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.legacy.core.command.ServiceCommand;
import com.quickblox.q_municate_core.legacy.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.legacy.service.QBService;
import com.quickblox.q_municate_core.legacy.service.QBServiceConsts;

public class QBInitChatServiceCommand extends ServiceCommand {

    private QBChatRestHelper chatRestHelper;

    public QBInitChatServiceCommand(Context context, QBChatRestHelper chatRestHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.INIT_CHAT_SERVICE_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {

        chatRestHelper.initChatService();

        return extras;
    }
}