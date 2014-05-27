package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.List;

public class QBLoadDialogMessagesCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBLoadDialogMessagesCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, QBDialog dialog) {
        Intent intent = new Intent(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_DIALOG, dialog);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_CHAT_DIALOG);
        List<QBHistoryMessage> messagesList = chatHelper.getDialogMessages(dialog);
        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_DIALOG_MESSAGES, (java.io.Serializable) messagesList);
        return bundle;
    }
}