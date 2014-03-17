package com.quickblox.qmunicate.qb.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.command.BaseCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.io.File;

public class QBUpdateUserCommand extends BaseCommand {

    private static final String TAG = QBUpdateUserCommand.class.getSimpleName();

    public static void start(Context context, QBUser user, File file) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_USER_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, file);
        context.startService(intent);
    }

    public QBUpdateUserCommand(Context context, String resultAction) {
        super(context, resultAction);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        QBUser user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        if (file != null) {
            QBFile qbFile = QBContent.uploadFileTask(file, true, (String) null);
            user.setFileId(qbFile.getId());
        }

        user = QBUsers.updateUser(user);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_USER, user);

        return result;
    }
}
