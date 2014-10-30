package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.model.ParcelableQBDialog;
import com.quickblox.q_municate.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.ChatDialogUtils;

import java.util.ArrayList;
import java.util.List;

public class QBJoinGroupDialogCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBJoinGroupDialogCommand(Context context, QBMultiChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, String roomJid) {
        ArrayList<String> roomJidList = new ArrayList<String>();
        roomJidList.add(roomJid);
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_LIST, roomJidList);
        context.startService(intent);
    }

    public static void start(Context context, ArrayList<ParcelableQBDialog> dialogList) {
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_ROOM_JID_LIST, dialogList);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<ParcelableQBDialog> dialogList = null;
        List<QBDialog> dialogs = null;
        if (extras != null && extras.containsKey(QBServiceConsts.EXTRA_ROOM_JID_LIST)) {
            dialogList = extras.getParcelableArrayList(QBServiceConsts.EXTRA_ROOM_JID_LIST);
            dialogs = ChatDialogUtils.parcelableDialogsToDialogs(dialogList);
        }
        if(dialogs == null) {
            dialogs = DatabaseManager.getDialogs(context);
        }
        if (dialogs != null && !dialogs.isEmpty()) {
            multiChatHelper.tryJoinRoomChats(dialogs);
        }
        return extras;
    }
}