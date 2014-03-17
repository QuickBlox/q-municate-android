package com.quickblox.qmunicate.qb.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.BaseCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBLoginCommand extends BaseCommand {

    private static final String TAG = QBLoginCommand.class.getSimpleName();

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        context.startService(intent);
    }

    public QBLoginCommand(Context context, String resultAction) {
        super(context, resultAction);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);

        QBAuth.createSession();
        user = QBUsers.signIn(user);
        if (user != null) {
            Log.d(TAG, "login successful with " + user.toString());
        } else {
            Log.d(TAG, "user is null");
        }
        // QBChatService.getInstance().loginWithUser(user);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}
