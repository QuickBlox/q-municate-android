package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBChatHistoryMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.util.List;

public class QBLoadDialogMessagesCommand extends ServiceCommand {

    private QBBaseChatHelper baseChatHelper;

    public QBLoadDialogMessagesCommand(Context context, QBBaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
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

        List<QBChatHistoryMessage> dialogMessagesList = baseChatHelper.getDialogMessages(dialog, lastDateLoad);

        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_DIALOG_MESSAGES,
                (java.io.Serializable) dialogMessagesList);
        return bundle;
    }
}