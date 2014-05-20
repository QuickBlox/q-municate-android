package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBSendPrivateChatMessageCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBSendPrivateChatMessageCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String message) {
        Intent intent = new Intent(QBServiceConsts.SEND_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, message);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        chatHelper.sendPrivateMessage(message);
        return null;
    }
}