package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBAddFriendCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBAddFriendCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, Friend friend) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, friend);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Friend friend = (Friend) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        friendListHelper.inviteFriend(friend.getId());

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND, friend);

        return result;
    }
}
