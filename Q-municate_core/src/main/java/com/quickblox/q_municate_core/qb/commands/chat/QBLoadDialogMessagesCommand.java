package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.Consts;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.List;

public class QBLoadDialogMessagesCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBLoadDialogMessagesCommand(Context context, QBChatHelper chatHelper, String successAction,
                                       String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, QBChatDialog dialog, long lastDateLoad, boolean isLoadOldMessages) {
        Intent intent = new Intent(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY, lastDateLoad);
        intent.putExtra(QBServiceConsts.EXTRA_LOAD_MORE, isLoadOldMessages);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBChatDialog dialog = (QBChatDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        long lastDateLoad = extras.getLong(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY);
        boolean isLoadOldMessages = extras.getBoolean(QBServiceConsts.EXTRA_LOAD_MORE);

        Bundle returnedBundle = new Bundle();
        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(ConstsCore.DIALOG_MESSAGES_PER_PAGE);

        if (isLoadOldMessages) {
            messageGetBuilder.lt(Consts.MESSAGE_DATE_SENT, lastDateLoad);
            messageGetBuilder.sortDesc(QBServiceConsts.EXTRA_DATE_SENT);
        } else {
            messageGetBuilder.gt(Consts.MESSAGE_DATE_SENT, lastDateLoad);
            if (lastDateLoad > 0) {
                messageGetBuilder.sortAsc(QBServiceConsts.EXTRA_DATE_SENT);
            } else {
                messageGetBuilder.sortDesc(QBServiceConsts.EXTRA_DATE_SENT);
            }
        }

        messageGetBuilder.markAsRead(false);

        List<QBChatMessage> dialogMessagesList = chatHelper.getDialogMessages(messageGetBuilder,
                returnedBundle, dialog, lastDateLoad);

        Bundle bundleResult = new Bundle();
        bundleResult.putString(QBServiceConsts.EXTRA_DIALOG_ID, dialog.getDialogId());
        bundleResult.putBoolean(QBServiceConsts.EXTRA_IS_LOAD_OLD_MESSAGES, isLoadOldMessages);
        bundleResult.putLong(QBServiceConsts.EXTRA_LAST_DATE_LOAD_MESSAGES, lastDateLoad);
        bundleResult.putSerializable(QBServiceConsts.EXTRA_DIALOG_MESSAGES, (java.io.Serializable) dialogMessagesList);
        bundleResult.putInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES, dialogMessagesList != null
                ?  dialogMessagesList.size() : ConstsCore.ZERO_INT_VALUE);

        return bundleResult;
    }
}