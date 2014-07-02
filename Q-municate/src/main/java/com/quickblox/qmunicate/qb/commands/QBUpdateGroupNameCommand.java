package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBMultiChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBUpdateGroupNameCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBUpdateGroupNameCommand(Context context, QBMultiChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, String dialogId, String newName) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_GROUP_NAME_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_GROUP_NAME, newName);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        String newName = extras.getString(QBServiceConsts.EXTRA_GROUP_NAME);

        multiChatHelper.updateRoomName(dialogId, newName);

        return extras;
    }
}