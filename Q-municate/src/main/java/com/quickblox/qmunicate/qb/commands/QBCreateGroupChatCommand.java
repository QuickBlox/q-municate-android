package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.List;

public class QBCreateGroupChatCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBCreateGroupChatCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String roomName, List<Friend> friends) {
        Integer[] friendIds = getFriendIds(friends);
        Intent intent = new Intent(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_NAME, roomName);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendIds);
        context.startService(intent);
    }

    private static Integer[] getFriendIds(List<Friend> friends) {
        Integer[] friendIds = new Integer[friends.size()];
        for (int index = 0; index < friends.size(); index++) {
            friendIds[index] = friends.get(index).getId();
        }
        return friendIds;
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Integer[] friendIds = (Integer[]) extras.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
        String roomName = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_NAME);

        chatHelper.initRoomChat(roomName, friendIds);

        return extras;
    }
}