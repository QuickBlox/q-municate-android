package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.qb.helpers.BaseChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBUpdateStatusMessageReadCommand extends ServiceCommand {

    private BaseChatHelper baseChatHelper;

    public QBUpdateStatusMessageReadCommand(Context context, BaseChatHelper baseChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, QBDialog dialog, MessageCache messageCache) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_READ_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, messageCache);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        MessageCache messageCache = (MessageCache) extras.getSerializable(QBServiceConsts.EXTRA_MESSAGE);

        baseChatHelper.updateStatusMessageRead(dialog.getDialogId(), messageCache);

        return null;
    }
}