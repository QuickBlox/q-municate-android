package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ErrorUtils;

public class QBUpdateStatusMessageCommand extends ServiceCommand {

    private static String TAG = QBUpdateStatusMessageCommand.class.getName();

    private QBBaseChatHelper baseChatHelper;

    public QBUpdateStatusMessageCommand(Context context, QBBaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, QBDialog dialog, MessageCache messageCache, boolean forPrivate) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, messageCache);
        intent.putExtra(QBServiceConsts.EXTRA_IS_FOR_PRIVATE, forPrivate);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        MessageCache messageCache = (MessageCache) extras.getSerializable(QBServiceConsts.EXTRA_MESSAGE);
        boolean forPrivate = extras.getBoolean(QBServiceConsts.EXTRA_IS_FOR_PRIVATE);

        try {
            baseChatHelper.updateStatusMessageRead(dialog.getDialogId(), messageCache, forPrivate);
        } catch (Exception e) {
            ErrorUtils.logError(TAG, e + " --- dialogId = " + dialog.getDialogId()
                    + ", messageId = " + messageCache.getId());
        }

        return null;
    }
}