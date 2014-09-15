package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBLoadUserCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBLoadUserCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, int userId) {
        Intent intent = new Intent(QBServiceConsts.LOAD_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        int userId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);

        User user = friendListHelper.loadUser(userId);

        if (user != null) {
            DatabaseManager.saveUser(context, user);
        }

        return null;
    }
}