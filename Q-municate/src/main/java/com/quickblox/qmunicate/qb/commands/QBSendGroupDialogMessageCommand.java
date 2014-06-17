package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBSendGroupDialogMessageCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBSendGroupDialogMessageCommand(Context context, QBChatHelper ChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = ChatHelper;
    }

    public static void start(Context context, String roomJidId, String message, QBFile file) {
        Intent intent = new Intent(QBServiceConsts.SEND_GROUP_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, roomJidId);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, message);
        intent.putExtra(QBServiceConsts.EXTRA_QBFILE, file);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String roomJidId = extras.getString(QBServiceConsts.EXTRA_ROOM_JID);
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        QBFile file = (QBFile) extras.getSerializable(QBServiceConsts.EXTRA_QBFILE);

        if(file == null) {
            chatHelper.sendGroupMessage(roomJidId, message);
        } else {
            chatHelper.sendGroupMessageWithAttachImage(roomJidId, file);
        }

        return null;
    }
}