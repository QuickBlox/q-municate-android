package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBGetCountUnreadChatsDialogsCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBGetCountUnreadChatsDialogsCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.GET_COUNT_UNREAD_CHATS_DIALOGS_ACTION, null, context,
                QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        int counter = chatHelper.getCountUnreadChatsDialogs();
        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_COUNT_UNREAD_CHATS_DIALOGS, counter);
        return bundle;
    }
}