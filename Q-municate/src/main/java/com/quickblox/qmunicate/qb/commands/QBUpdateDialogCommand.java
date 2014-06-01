package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Dialog;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBUpdateDialogCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBUpdateDialogCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, QBDialog dialog, String roomJidId) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_CHAT_DIALOG_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_ID, roomJidId);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TEMP_DIALOG, false);
        context.startService(intent);
    }

    public static void start(Context context, Dialog dialog, String roomJidId, boolean isTempDialog) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_CHAT_DIALOG_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_ID, roomJidId);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TEMP_DIALOG, isTempDialog);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        boolean isTempDialog = extras.getBoolean(QBServiceConsts.EXTRA_IS_TEMP_DIALOG);
        String roomJidId = extras.getString(QBServiceConsts.EXTRA_ROOM_JID_ID);

        if (isTempDialog) {
            Dialog dialog = (Dialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            chatHelper.updateTempDialog(dialog, roomJidId);
        } else {
            QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
            chatHelper.updateDialog(dialog, roomJidId);
        }

        return null;
    }
}