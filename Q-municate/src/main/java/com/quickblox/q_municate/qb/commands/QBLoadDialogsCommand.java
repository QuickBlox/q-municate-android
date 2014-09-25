package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.ParcelableQBDialog;
import com.quickblox.q_municate.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.ChatDialogUtils;

import java.util.ArrayList;
import java.util.List;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBChatRestHelper chatRestHelper;
    private QBMultiChatHelper multiChatHelper;

    public QBLoadDialogsCommand(Context context, QBChatRestHelper chatRestHelper,
            QBMultiChatHelper multiChatHelper, String successAction, String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        List<QBDialog> dialogsList = chatRestHelper.getDialogs();
        ArrayList<ParcelableQBDialog> parcelableQBDialog = null;

        if (dialogsList != null && !dialogsList.isEmpty()) {
            parcelableQBDialog = ChatDialogUtils.dialogsToParcelableDialogs(dialogsList);
            DatabaseManager.saveDialogs(context, dialogsList);
            multiChatHelper.tryJoinRoomChats(dialogsList);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS,  parcelableQBDialog);

        return bundle;
    }
}