package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.ParcelableQBDialog;
import com.quickblox.qmunicate.qb.helpers.QBMultiChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatDialogUtils;

import java.util.ArrayList;

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
        if (extras != null && extras.containsKey(QBServiceConsts.EXTRA_ROOM_JID_LIST)) {
            dialogList = extras.getParcelableArrayList(QBServiceConsts.EXTRA_ROOM_JID_LIST);
        }
        if (dialogList != null && !dialogList.isEmpty()) {
            ArrayList<QBDialog> dialogs = ChatDialogUtils.parcelableDialogsToDialogs(dialogList);
            multiChatHelper.tryJoinRoomChats(dialogs);
        }
        return extras;
    }
}