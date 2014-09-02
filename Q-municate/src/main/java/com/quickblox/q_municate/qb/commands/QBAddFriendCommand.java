package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBAddFriendCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBAddFriendCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, User friend) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, friend);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        User friend = (User) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        friendListHelper.inviteFriend(friend.getUserId());

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND, friend);

        return result;
    }
}
