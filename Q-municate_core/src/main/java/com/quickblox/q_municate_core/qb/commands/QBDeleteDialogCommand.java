package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBDeleteDialogCommand extends ServiceCommand {

    private QBBaseChatHelper baseChatHelper;

    public QBDeleteDialogCommand(Context context, QBBaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, String dialogId, QBDialogType dialogType) {
        Intent intent = new Intent(QBServiceConsts.DELETE_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_TYPE, dialogType);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws QBResponseException {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        QBDialogType dialogType = (QBDialogType) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG_TYPE);
        baseChatHelper.deleteDialog(dialogId, dialogType);
        return extras;
    }
}