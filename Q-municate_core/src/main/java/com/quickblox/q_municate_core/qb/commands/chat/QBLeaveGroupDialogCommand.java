package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.Dialog;

public class QBLeaveGroupDialogCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBLeaveGroupDialogCommand(Context context, QBChatHelper chatHelper, String successAction,
                                     String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, Dialog dialog) {
        Intent intent = new Intent(QBServiceConsts.LEAVE_GROUP_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Dialog dialog = (Dialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        chatHelper.leaveRoomChat(dialog);

        return extras;
    }
}