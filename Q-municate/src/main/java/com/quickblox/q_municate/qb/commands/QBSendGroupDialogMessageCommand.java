package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBSendGroupDialogMessageCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBSendGroupDialogMessageCommand(Context context, QBMultiChatHelper multiChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
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

        if (file == null) {
            multiChatHelper.sendGroupMessage(roomJidId, message);
        } else {
            multiChatHelper.sendGroupMessageWithAttachImage(roomJidId, file);
        }

        return null;
    }
}