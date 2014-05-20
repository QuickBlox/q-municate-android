package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;

import java.io.File;

public class QBUpdateUserCommand extends ServiceCommand {

    private static final String TAG = QBUpdateUserCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;
    private final QBFriendListHelper friendListHelper;

    public QBUpdateUserCommand(Context context, QBAuthHelper authHelper, QBFriendListHelper friendListHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context, QBUser user, File file, String status) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, file);
        intent.putExtra(QBServiceConsts.EXTRA_STATUS, status);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);
        String status = extras.getString(QBServiceConsts.EXTRA_STATUS);

        user.setOldPassword(user.getPassword());
        updateUser(user, file, status);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }

    private void updateUser(QBUser user, File file,
            String status) throws QBResponseException, SmackException.NotConnectedException {
        if (file == null) {
            authHelper.updateUser(user);
        } else {
            authHelper.updateUser(user, file);
        }
        friendListHelper.sendStatus(status);
    }
}
