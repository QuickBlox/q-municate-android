package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBJoinGroupChatCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBJoinGroupChatCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String roomJid) {
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, roomJid);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String roomJid = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_JID);
        chatHelper.joinRoomChat(roomJid);

        return extras;
    }
}