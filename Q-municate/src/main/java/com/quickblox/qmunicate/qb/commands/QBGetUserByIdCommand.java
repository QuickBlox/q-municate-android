package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBGetUserByIdCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBGetUserByIdCommand(Context context, QBChatHelper chatHelper, String successAction,
                                String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, int userId) {
        Intent intent = new Intent(QBServiceConsts.GET_USER_BY_ID_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        int userId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);

        QBUser user = chatHelper.getUserById(userId);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}