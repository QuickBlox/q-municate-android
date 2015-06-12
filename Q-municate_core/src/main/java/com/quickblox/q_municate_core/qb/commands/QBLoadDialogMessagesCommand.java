package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBBaseChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.List;

public class QBLoadDialogMessagesCommand extends ServiceCommand {

    private QBBaseChatHelper baseChatHelper;

    public QBLoadDialogMessagesCommand(Context context, QBBaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, QBDialog dialog, long lastDateLoad, int skipMessages) {
        Intent intent = new Intent(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY, lastDateLoad);
        intent.putExtra(QBServiceConsts.EXTRA_SKIP_ITEMS, skipMessages);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        long lastDateLoad = extras.getLong(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY);
        int skipMessages = extras.getInt(QBServiceConsts.EXTRA_SKIP_ITEMS);

        Bundle returnedBundle = new Bundle();
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesSkip(skipMessages);
        customObjectRequestBuilder.setPagesLimit(ConstsCore.DIALOG_MESSAGES_PER_PAGE);

        List<QBChatMessage> dialogMessagesList = baseChatHelper.getDialogMessages(customObjectRequestBuilder,
                returnedBundle, dialog, lastDateLoad);

        Bundle bundleResult = new Bundle();
        bundleResult.putSerializable(QBServiceConsts.EXTRA_DIALOG_MESSAGES,
                (java.io.Serializable) dialogMessagesList);
        bundleResult.putInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES,
                returnedBundle.getInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES));

        return bundleResult;
    }
}