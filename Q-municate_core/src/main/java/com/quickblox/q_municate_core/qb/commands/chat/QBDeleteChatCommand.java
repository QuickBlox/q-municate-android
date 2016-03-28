package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.models.Dialog;

public class QBDeleteChatCommand extends ServiceCommand {

    private QBBaseChatHelper baseChatHelper;

    public QBDeleteChatCommand(Context context, QBBaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, String dialogId, Dialog.Type dialogType) {
        Intent intent = new Intent(QBServiceConsts.DELETE_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_TYPE, dialogType);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        Dialog.Type dialogType = (Dialog.Type) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG_TYPE);
        baseChatHelper.deleteDialog(dialogId, dialogType);
        return extras;
    }
}