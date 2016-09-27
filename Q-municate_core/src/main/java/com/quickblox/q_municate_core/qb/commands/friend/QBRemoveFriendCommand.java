package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBRemoveFriendCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBRemoveFriendCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, int friendId) {
        Intent intent = new Intent(QBServiceConsts.REMOVE_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND_ID, friendId);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        int friendId = extras.getInt(QBServiceConsts.EXTRA_FRIEND_ID);

        friendListHelper.removeFriend(friendId);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND_ID, friendId);

        return result;
    }
}