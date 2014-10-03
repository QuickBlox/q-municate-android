package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.helpers.QBRestHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

import java.util.Collection;
import java.util.List;

public class QBLoadUserCommand extends ServiceCommand {

    private QBRestHelper restHelper;

    public QBLoadUserCommand(Context context, QBRestHelper restHelper, String successAction,
                             String failAction) {
        super(context, successAction, failAction);
        this.restHelper = restHelper;
    }

    public static void start(Context context, int userId) {
        Intent intent = new Intent(QBServiceConsts.LOAD_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        context.startService(intent);
    }

    public static void start(Context context, List<Integer> userIdsList) {
        Intent intent = new Intent(QBServiceConsts.LOAD_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USERS, (java.io.Serializable) userIdsList);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        if (extras.containsKey(QBServiceConsts.EXTRA_USERS)) {
            Collection<Integer> userIdsList = (List<Integer>) extras.getSerializable(QBServiceConsts.EXTRA_USERS);
            Collection<User> usersList = restHelper.loadUsers(userIdsList);
            if (usersList != null) {
                DatabaseManager.saveUsers(context, usersList);
            }
        } else if (extras.containsKey(QBServiceConsts.EXTRA_USER_ID)) {
            int userId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
            User user = restHelper.loadUser(userId);
            if (user != null) {
                DatabaseManager.saveUser(context, user);
            }
        }

        return null;
    }
}