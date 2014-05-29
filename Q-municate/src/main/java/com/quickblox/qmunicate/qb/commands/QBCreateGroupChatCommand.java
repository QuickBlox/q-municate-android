package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatUtils;

import java.util.ArrayList;
import java.util.List;

public class QBCreateGroupChatCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBCreateGroupChatCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String roomName, ArrayList<Friend> friendList) {
        Intent intent = new Intent(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_NAME, roomName);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendList);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<Friend> friendList = (ArrayList<Friend>) extras.getSerializable(
                QBServiceConsts.EXTRA_FRIENDS);
        String roomName = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_NAME);

        QBDialog dialog = chatHelper.createRoomChat(roomName, getFriendIdsList(friendList));
        extras.putSerializable(QBServiceConsts.EXTRA_CHAT_DIALOG, ChatUtils.getChatCacheFromQBDialog(dialog));
        return extras;
    }

    private ArrayList<Integer> getFriendIdsList(List<Friend> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (Friend friend : friendList) {
            friendIdsList.add(friend.getId());
        }
        return friendIdsList;
    }
}