package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBUpdateChatDialogCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBUpdateChatDialogCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, int opponentId, String lastMessage, int countMessage) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_CHAT_DIALOG_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT_ID, opponentId);
        intent.putExtra(QBServiceConsts.EXTRA_LAST_CHAT_MESSAGE, lastMessage);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_COUNT_UNREAD_MESSAGE, countMessage);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        int opponentId = extras.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        String lastMessage = extras.getString(QBServiceConsts.EXTRA_LAST_CHAT_MESSAGE);
        int countMessage = extras.getInt(QBServiceConsts.EXTRA_DIALOG_COUNT_UNREAD_MESSAGE);
        chatHelper.updateLoadedChatDialog(opponentId, lastMessage, countMessage);
        return null;
    }
}