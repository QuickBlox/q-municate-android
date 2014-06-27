package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBPrivateChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBSendPrivateChatMessageCommand extends ServiceCommand {

    private QBPrivateChatHelper privateChatHelper;

    public QBSendPrivateChatMessageCommand(Context context, QBPrivateChatHelper privateChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.privateChatHelper = privateChatHelper;
    }

    public static void start(Context context, String message, int userId, QBFile file) {
        Intent intent = new Intent(QBServiceConsts.SEND_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, message);
        intent.putExtra(QBServiceConsts.EXTRA_QBFILE, file);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, userId);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        int userId = extras.getInt(QBServiceConsts.EXTRA_FRIEND_ID);
        QBFile file = (QBFile) extras.getSerializable(QBServiceConsts.EXTRA_QBFILE);

        if (file == null) {
            privateChatHelper.sendPrivateMessage(message, userId);
        } else {
            privateChatHelper.sendPrivateMessageWithAttachImage(file, userId);
        }

        return null;
    }
}