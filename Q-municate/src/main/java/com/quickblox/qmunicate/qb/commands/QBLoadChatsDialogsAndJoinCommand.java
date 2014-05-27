package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.CompositeServiceCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.io.File;

public class QBLoadChatsDialogsAndJoinCommand extends CompositeServiceCommand {

    public QBLoadChatsDialogsAndJoinCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, QBUser user, File image) {
        Intent intent = new Intent(QBServiceConsts.LOAD_AND_JOIN, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, image);
        context.startService(intent);
    }
}