package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatDialogUtils;

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
            multiChatHelper.tryJoinRoomChats(dialogsList);
        }

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS,  parcelableQBDialog);

        return bundle;
    }
}