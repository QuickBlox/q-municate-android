package com.quickblox.q_municate_core.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBInitFriendListCommand extends ServiceCommand {

    private final static String TAG = QBInitFriendListCommand.class.getSimpleName();

    private QBFriendListHelper friendListHelper;
    private QBChatHelper chatHelper;

    public QBInitFriendListCommand(Context context, QBFriendListHelper friendListHelper, QBChatHelper chatHelper, String successAction,
                                   String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
        this.chatHelper = chatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.INIT_FRIEND_LIST_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        friendListHelper.init(chatHelper);
        return extras;
    }
}