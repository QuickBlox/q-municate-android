package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBAddFriendCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBAddFriendCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, int userId) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        int userId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);

        friendListHelper.addFriend(userId);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND_ID, userId);

        return result;
    }
}