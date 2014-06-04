package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBUpdateStatusMessageCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBUpdateStatusMessageCommand(Context context, QBChatHelper chatHelper, String successAction,
                                        String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String messageId, boolean isRead) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(QBServiceConsts.EXTRA_STATUS_MESSAGE, isRead);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String messageId = extras.getString(QBServiceConsts.EXTRA_MESSAGE_ID);
        boolean isRead = extras.getBoolean(QBServiceConsts.EXTRA_STATUS_MESSAGE);
        chatHelper.updateStatusMessage(messageId, isRead);
        return null;
    }
}