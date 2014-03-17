package com.quickblox.qmunicate.qb;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.auth.QBAuth;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.io.File;

public class QBSignUpCommand extends ServiceCommand {

    private static final String TAG = QBSignUpCommand.class.getSimpleName();

    public static void start(Context context, QBUser user, File image) {
        Intent intent = new Intent(QBServiceConsts.SIGNUP_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, image);
        context.startService(intent);
    }

    public QBSignUpCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        QBAuth.createSession();
        user = QBUsers.signUpSignInTask(user);
        if (file != null) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
            user.setFileId(qbFile.getId());
            user = QBUsers.updateUser(user);
        }

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}