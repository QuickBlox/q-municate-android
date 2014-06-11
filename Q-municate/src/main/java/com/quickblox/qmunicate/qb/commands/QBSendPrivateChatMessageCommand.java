package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBSendPrivateChatMessageCommand extends ServiceCommand {

    private QBChatHelper qbChatHelper;

    public QBSendPrivateChatMessageCommand(Context context, QBChatHelper qbChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.qbChatHelper = qbChatHelper;
    }

    public static void start(Context context, String message, QBFile file) {
        Intent intent = new Intent(QBServiceConsts.SEND_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, message);
        intent.putExtra(QBServiceConsts.EXTRA_QBFILE, file);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        QBFile file = (QBFile) extras.getSerializable(QBServiceConsts.EXTRA_QBFILE);

        if(file == null) {
            qbChatHelper.sendPrivateMessage(message);
        } else {
            qbChatHelper.sendPrivateMessageWithAttachImage(file);
        }

        return null;
    }
}