package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.BaseChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBSendTypingStatusCommand extends ServiceCommand {

    private BaseChatHelper baseChatHelper;

    public QBSendTypingStatusCommand(Context context, BaseChatHelper baseChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.baseChatHelper = baseChatHelper;
    }

    public static void start(Context context, int opponentId, boolean isTyping) {
        Intent intent = new Intent(QBServiceConsts.SEND_TYPING_STATUS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_OPPONENT_ID, opponentId);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TYPING, isTyping);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        int opponentId = extras.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        boolean isTyping = extras.getBoolean(QBServiceConsts.EXTRA_IS_TYPING);

        if (isTyping) {
            baseChatHelper.sendIsTypingToServer(opponentId);
        } else {
            baseChatHelper.sendStopTypingToServer(opponentId);
        }

        return null;
    }
}