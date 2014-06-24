package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.ArrayList;
import java.util.List;

public class QBJoinGroupDialogCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBJoinGroupDialogCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String roomJid) {
        ArrayList<String> roomJidList = new ArrayList<String>();
        roomJidList.add(roomJid);
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_LIST, roomJidList);
        context.startService(intent);
    }

    public static void start(Context context, List<QBDialog> roomJidList) {
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_LIST, new ArrayList<QBDialog>(roomJidList));
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        List<QBDialog> dialogs = null;
        if (extras != null && extras.containsKey(QBServiceConsts.EXTRA_ROOM_JID_LIST)) {
            dialogs = (ArrayList<QBDialog>) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_JID_LIST);
        } else {
            dialogs = DatabaseManager.getDialogs(context);
        }

        if (dialogs != null && !dialogs.isEmpty()) {
            for (QBDialog dialog : dialogs) {
                chatHelper.joinRoomChat(dialog);
            }
        }
        return extras;
    }
}