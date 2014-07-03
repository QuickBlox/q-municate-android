package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.ParcelableQBDialog;
import com.quickblox.qmunicate.qb.helpers.QBChatRestHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatDialogUtils;

import java.util.ArrayList;

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
        ArrayList<ParcelableQBDialog> dialogsList = ChatDialogUtils.dialogsToParcelableDialogs(chatRestHelper.getDialogs());
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS,  dialogsList);
        return bundle;
    }
}