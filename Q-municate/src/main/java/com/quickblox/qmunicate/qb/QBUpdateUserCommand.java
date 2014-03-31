package com.quickblox.qmunicate.qb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.service.QBAuthHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.io.File;

public class QBUpdateUserCommand extends ServiceCommand {

    private static final String TAG = QBUpdateUserCommand.class.getSimpleName();

    private final QBAuthHelper qbAuthHelper;

    public QBUpdateUserCommand(Context context, QBAuthHelper qbAuthHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.qbAuthHelper = qbAuthHelper;
    }

    public static void start(Context context, QBUser user, File file) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, file);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        user.setOldPassword(user.getPassword());
        qbAuthHelper.updateUser(user, file);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}
