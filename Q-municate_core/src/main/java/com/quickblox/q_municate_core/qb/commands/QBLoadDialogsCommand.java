package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public class QBLoadDialogsCommand extends ServiceCommand {

    private QBGroupChatHelper multiChatHelper;

    public QBLoadDialogsCommand(Context context, QBGroupChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        List<QBDialog> dialogsList = multiChatHelper.getDialogs();
        List<ParcelableQBDialog> parcelableQBDialog = new ArrayList<>();

        if (dialogsList != null && !dialogsList.isEmpty()) {
            parcelableQBDialog = ChatUtils.qbDialogsToParcelableQBDialogs(dialogsList);
            multiChatHelper.tryJoinRoomChats(dialogsList);
        }

        if (extras == null) {
            extras = new Bundle();
        }

        extras.putParcelableArrayList(QBServiceConsts.EXTRA_CHATS_DIALOGS, (ArrayList<? extends Parcelable>) parcelableQBDialog);

        return extras;
    }
}