package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBGetTypingStatusCommand extends ServiceCommand {

    public QBGetTypingStatusCommand(Context context, String successAction,
            String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, boolean isTyping) {
        Intent intent = new Intent(QBServiceConsts.GET_TYPING_STATUS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TYPING, isTyping);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        return extras;
    }
}