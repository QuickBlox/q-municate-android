package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBVideoChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.io.File;

public class QBSignUpCommand extends ServiceCommand {

    private static final String TAG = QBSignUpCommand.class.getSimpleName();

    private final QBAuthHelper qbAuthHelper;
    private final QBVideoChatHelper videoChatHelper;

    public QBSignUpCommand(Context context, QBAuthHelper qbAuthHelper, QBVideoChatHelper videoChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.qbAuthHelper = qbAuthHelper;
        this.videoChatHelper = videoChatHelper;
    }

    public static void start(Context context, QBUser user, File image) {
        Intent intent = new Intent(QBServiceConsts.SIGNUP_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, image);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        user = qbAuthHelper.signup(user, file);
        videoChatHelper.init(context);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}