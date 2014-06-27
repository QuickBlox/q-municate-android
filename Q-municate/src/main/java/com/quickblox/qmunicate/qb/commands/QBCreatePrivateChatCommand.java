package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.helpers.QBPrivateChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBCreatePrivateChatCommand extends ServiceCommand {

    private QBPrivateChatHelper chatHelper;

    public QBCreatePrivateChatCommand(Context context, QBPrivateChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, Friend friend) {
        Intent intent = new Intent(QBServiceConsts.CREATE_PRIVATE_CHAT_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, friend.getId());
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Integer friendId = (Integer) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        QBDialog privateDialog = chatHelper.createPrivateChatOnRest(friendId);
        extras.putSerializable(QBServiceConsts.EXTRA_DIALOG, privateDialog);
        return extras;
    }
}