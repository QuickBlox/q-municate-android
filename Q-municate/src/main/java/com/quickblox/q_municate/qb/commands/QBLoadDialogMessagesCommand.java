package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBChatHistoryMessage;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

import java.util.List;

public class QBLoadDialogMessagesCommand extends ServiceCommand {

    private QBChatRestHelper chatRestHelper;

    public QBLoadDialogMessagesCommand(Context context, QBChatRestHelper chatRestHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
    }

    public static void start(Context context, QBDialog dialog, long lastDateLoad) {
        Intent intent = new Intent(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY, lastDateLoad);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        long lastDateLoad = extras.getLong(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY);
        List<QBChatHistoryMessage> dialogMessagesList = chatRestHelper.getDialogMessages(dialog, lastDateLoad);
        if (dialogMessagesList != null) {
            DatabaseManager.saveChatMessages(context, dialogMessagesList, dialog.getDialogId());
        }
        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_DIALOG_MESSAGES,
                (java.io.Serializable) dialogMessagesList);
        return bundle;
    }
}