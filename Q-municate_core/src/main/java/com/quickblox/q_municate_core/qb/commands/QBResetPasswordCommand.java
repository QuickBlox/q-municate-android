package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBResetPasswordCommand extends ServiceCommand {

    private static final String TAG = QBResetPasswordCommand.class.getSimpleName();

    private final QBAuthHelper authHelper;

    public QBResetPasswordCommand(Context context, QBAuthHelper authHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
    }

    public static void start(Context context, String email) {
        Intent intent = new Intent(QBServiceConsts.RESET_PASSWORD_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_EMAIL, email);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String email = extras.getString(QBServiceConsts.EXTRA_EMAIL);

        authHelper.resetPassword(email);

        Bundle result = new Bundle();
        result.putString(QBServiceConsts.EXTRA_EMAIL, email);

        return result;
    }
}