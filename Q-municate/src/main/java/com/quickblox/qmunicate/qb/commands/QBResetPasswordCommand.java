package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBResetPasswordCommand extends ServiceCommand {

    private static final String TAG = QBResetPasswordCommand.class.getSimpleName();

    private final QBAuthHelper qbAuthHelper;

    public QBResetPasswordCommand(Context context, QBAuthHelper qbAuthHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.qbAuthHelper = qbAuthHelper;
    }

    public static void start(Context context, String email) {
        Intent intent = new Intent(QBServiceConsts.RESET_PASSWORD_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_EMAIL, email);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String email = extras.getString(QBServiceConsts.EXTRA_EMAIL);

        qbAuthHelper.resetPassword(email);

        Bundle result = new Bundle();
        result.putString(QBServiceConsts.EXTRA_EMAIL, email);

        return result;
    }
}