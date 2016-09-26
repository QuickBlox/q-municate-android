package com.quickblox.q_municate_core.legacy.qb.commands.friend;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.legacy.core.command.ServiceCommand;
import com.quickblox.q_municate_core.legacy.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.legacy.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.legacy.service.QBService;
import com.quickblox.q_municate_core.legacy.service.QBServiceConsts;

public class QBInitFriendListCommand extends ServiceCommand {

    private final static String TAG = QBInitFriendListCommand.class.getSimpleName();

    private QBFriendListHelper friendListHelper;
    private QBPrivateChatHelper privateChatHelper;

    public QBInitFriendListCommand(Context context, QBFriendListHelper friendListHelper, QBPrivateChatHelper privateChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
        this.privateChatHelper = privateChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.INIT_FRIEND_LIST_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        friendListHelper.init(privateChatHelper);
        return extras;
    }
}