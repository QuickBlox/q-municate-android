package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.io.File;

public class QBSignUpRestCommand extends CompositeServiceCommand {

    private static final String TAG = QBSignUpRestCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBSignUpRestCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context, QBUser user, File image) {
        Intent intent = new Intent(QBServiceConsts.SIGNUP_REST_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, image);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        try{
            user = authHelper.signup(user, file);
        } catch (BaseServiceException e){
            throw new QBResponseException(e.getLocalizedMessage());
        }
        extras.putSerializable(QBServiceConsts.EXTRA_USER, user);
        return extras;
    }
}