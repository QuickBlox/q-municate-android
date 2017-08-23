package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class QBAddFriendCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBAddFriendCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, int userId) {
        List<Integer> friendsIds = Collections.singletonList(userId);
        start(context, friendsIds);
    }

    public static void start(Context context, List<Integer> usersIds) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USERS_IDS, (Serializable) usersIds);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        List<Integer> usersIds = (List<Integer>) extras.getSerializable(QBServiceConsts.EXTRA_USERS_IDS);

        friendListHelper.addFriends(usersIds);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USERS_IDS, (Serializable) usersIds);

        return result;
    }
}