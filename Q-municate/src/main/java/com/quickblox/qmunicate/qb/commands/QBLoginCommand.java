package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.qb.helpers.QBVideoChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBLoginCommand extends ServiceCommand {

    private static final String TAG = QBLoginCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;
    private final QBFriendListHelper friendListHelper;
    private final QBVideoChatHelper videoChatHelper;

    public QBLoginCommand(Context context, QBAuthHelper authHelper, QBFriendListHelper friendListHelper,
            QBVideoChatHelper videoChatHelper, String successAction, String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
        this.friendListHelper = friendListHelper;
        this.videoChatHelper = videoChatHelper;
    }

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);

        user = authHelper.login(user);
        friendListHelper.init();
        videoChatHelper.init(context);
        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        boolean loggedIn = QBChatService.getInstance().isLoggedIn();
        Log.d(TAG, String.valueOf(loggedIn));

        return result;
    }
}
