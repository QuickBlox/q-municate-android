package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.ArrayList;
import java.util.List;

public class QBImportFriendsCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBImportFriendsCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
                                  String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, List<Integer> userIdsArray) {
        Intent intent = new Intent(QBServiceConsts.IMPORT_FRIENDS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) userIdsArray);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<Integer> userIds = extras.getIntegerArrayList(QBServiceConsts.EXTRA_FRIENDS);

        for (int userId : userIds) {
            friendListHelper.inviteFriend(userId);
        }

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIENDS, userIds);

        return result;
    }
}