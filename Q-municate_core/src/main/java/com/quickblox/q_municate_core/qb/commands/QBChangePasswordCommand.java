package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBChangePasswordCommand extends ServiceCommand {

    private static final String TAG = QBChangePasswordCommand.class.getSimpleName();

    private final QBAuthHelper qbAuthHelper;

    public QBChangePasswordCommand(Context context, QBAuthHelper qbAuthHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.qbAuthHelper = qbAuthHelper;
    }

    public static void start(Context context, QBUser user) {
        Intent intent = new Intent(QBServiceConsts.CHANGE_PASSWORD_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);

        user = qbAuthHelper.updateUser(user);
        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}