package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.Consts;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
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

    public static void start(Context context, QBDialog dialog, long lastDateLoad, boolean loadMore) {
        Intent intent = new Intent(QBServiceConsts.LOAD_DIALOG_MESSAGES_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY, lastDateLoad);
        intent.putExtra(QBServiceConsts.EXTRA_LOAD_MORE, loadMore);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        long lastDateLoad = extras.getLong(QBServiceConsts.EXTRA_DATE_LAST_UPDATE_HISTORY);
        boolean loadMore = extras.getBoolean(QBServiceConsts.EXTRA_LOAD_MORE);

        Bundle returnedBundle = new Bundle();
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setLimit(ConstsCore.DIALOG_MESSAGES_PER_PAGE);

        if (loadMore) {
            customObjectRequestBuilder.lt(Consts.MESSAGE_DATE_SENT, lastDateLoad);
            customObjectRequestBuilder.sortDesc(QBServiceConsts.EXTRA_DATE_SENT);
        } else {
            customObjectRequestBuilder.gt(Consts.MESSAGE_DATE_SENT, lastDateLoad);
            if (lastDateLoad > 0) {
                customObjectRequestBuilder.sortAsc(QBServiceConsts.EXTRA_DATE_SENT);
            } else {
                customObjectRequestBuilder.sortDesc(QBServiceConsts.EXTRA_DATE_SENT);
            }
        }

        List<QBChatMessage> dialogMessagesList = baseChatHelper.getDialogMessages(customObjectRequestBuilder,
                returnedBundle, dialog, lastDateLoad);

        Bundle bundleResult = new Bundle();
        bundleResult.putSerializable(QBServiceConsts.EXTRA_DIALOG_MESSAGES, (java.io.Serializable) dialogMessagesList);
        bundleResult.putInt(QBServiceConsts.EXTRA_TOTAL_ENTRIES, dialogMessagesList != null
                ?  dialogMessagesList.size() : ConstsCore.ZERO_INT_VALUE);

        return bundleResult;
    }
}