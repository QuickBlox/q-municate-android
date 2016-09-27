package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;

public class QBCreateGroupDialogCommand extends ServiceCommand {

    private QBGroupChatHelper multiChatHelper;

    public QBCreateGroupDialogCommand(Context context, QBGroupChatHelper multiChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, String roomName, ArrayList<User> friendList, String photoUrl) {
        Intent intent = new Intent(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_NAME, roomName);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendList);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_PHOTO_URL, photoUrl);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<User> friendList = (ArrayList<User>) extras.getSerializable(QBServiceConsts.EXTRA_FRIENDS);
        String roomName = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_NAME);
        String photoUrl = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_PHOTO_URL);

        QBDialog dialog = multiChatHelper.createGroupChat(roomName, UserFriendUtils.getFriendIdsFromUsersList(friendList), photoUrl);
        extras.putSerializable(QBServiceConsts.EXTRA_DIALOG, dialog);
        return extras;
    }
}