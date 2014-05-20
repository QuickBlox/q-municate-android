package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBAddFriendsCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBAddFriendsCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, Integer[] users) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIENDS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, users);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Integer[] userIds = (Integer[]) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        for (Integer userId : userIds) {
            friendListHelper.inviteFriend(userId);
        }

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND, userIds);

        return result;
    }
}