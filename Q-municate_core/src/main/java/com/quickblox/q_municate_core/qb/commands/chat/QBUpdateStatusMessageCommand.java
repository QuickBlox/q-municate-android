package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.utils.ErrorUtils;

public class QBUpdateStatusMessageCommand extends ServiceCommand {

    private static String TAG = QBUpdateStatusMessageCommand.class.getName();

    private QBBaseChatHelper baseChatHelper;

    public QBUpdateStatusMessageCommand(Context context, QBBaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, QBDialog dialog, CombinationMessage combinationMessage, boolean forPrivate) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_STATUS_MESSAGE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_MESSAGE, combinationMessage);
        intent.putExtra(QBServiceConsts.EXTRA_IS_FOR_PRIVATE, forPrivate);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        CombinationMessage combinationMessage = (CombinationMessage) extras.getSerializable(QBServiceConsts.EXTRA_MESSAGE);
        boolean forPrivate = extras.getBoolean(QBServiceConsts.EXTRA_IS_FOR_PRIVATE);

        try {
            if (combinationMessage.getNotificationType() != null) {
                baseChatHelper.updateStatusNotificationMessageRead(dialog.getDialogId(), combinationMessage);
            } else {
                baseChatHelper.updateStatusMessageRead(dialog.getDialogId(), combinationMessage, forPrivate);
            }
        } catch (Exception e) {
            ErrorUtils.logError(TAG,
                    e + " --- dialogId = " + dialog.getDialogId() + ", messageId = " + combinationMessage
                            .getMessageId());
        }

        return null;
    }
}