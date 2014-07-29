package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.qb.helpers.BaseChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBUpdateStatusMessageCommand extends ServiceCommand {

    private BaseChatHelper baseChatHelper;

    public QBUpdateStatusMessageCommand(Context context, BaseChatHelper baseChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, QBDialog dialog, String messageId, long dateSent, boolean isRead) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(QBServiceConsts.EXTRA_DATE_SENT, dateSent);
        intent.putExtra(QBServiceConsts.EXTRA_STATUS_MESSAGE, isRead);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        String messageId = extras.getString(QBServiceConsts.EXTRA_MESSAGE_ID);
        long dateSent = extras.getLong(QBServiceConsts.EXTRA_DATE_SENT);
        boolean isRead = extras.getBoolean(QBServiceConsts.EXTRA_STATUS_MESSAGE);
        baseChatHelper.updateStatusMessage(dialog, messageId, dateSent, isRead);
        return null;
    }
}