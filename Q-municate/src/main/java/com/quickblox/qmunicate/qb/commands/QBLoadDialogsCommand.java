package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatRestHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.List;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBChatRestHelper chatRestHelper;

    public QBLoadDialogsCommand(Context context, QBChatRestHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        List<QBDialog> dialogsList = chatRestHelper.getDialogs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_CHATS_DIALOGS, (java.io.Serializable) dialogsList);
        return bundle;
    }
}