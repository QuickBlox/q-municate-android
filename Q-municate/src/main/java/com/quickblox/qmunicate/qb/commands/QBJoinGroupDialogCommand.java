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
import com.quickblox.qmunicate.utils.ChatUtils;

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

    public static void start(Context context, List<String> roomJidList) {
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_LIST, new ArrayList<String>(roomJidList));
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        List<String> roomJidList = null;
        if (extras != null && extras.containsKey(QBServiceConsts.EXTRA_ROOM_JID_LIST)) {
            roomJidList = (ArrayList<String>) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_JID_LIST);
        } else {
            List<QBDialog> dialogs = DatabaseManager.getDialogs(context);
            roomJidList = ChatUtils.getRoomJidListFromDialogs(dialogs);
        }

        for (String roomJid : roomJidList) {
            chatHelper.joinRoomChat(roomJid);
        }

        return extras;
    }
}