package com.quickblox.q_municate_core.legacy.qb.commands.rest;

import android.content.Context;
import android.content.Intent;

import com.quickblox.q_municate_core.legacy.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.legacy.service.QBService;
import com.quickblox.q_municate_core.legacy.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

import java.io.File;

public class QBSignUpCommand extends CompositeServiceCommand {

    public QBSignUpCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, QBUser user, File image) {
        Intent intent = new Intent(QBServiceConsts.SIGNUP_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, image);
        context.startService(intent);
    }
}