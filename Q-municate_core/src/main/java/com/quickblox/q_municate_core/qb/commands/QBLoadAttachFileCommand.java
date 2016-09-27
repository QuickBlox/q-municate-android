package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.io.File;

public class QBLoadAttachFileCommand extends ServiceCommand {

    private static final String TAG = QBLoadAttachFileCommand.class.getSimpleName();

    private final QBPrivateChatHelper privateChatHelper;

    public QBLoadAttachFileCommand(Context context, QBPrivateChatHelper privateChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.privateChatHelper = privateChatHelper;
    }

    public static void start(Context context, File file, String dialogId) {
        Intent intent = new Intent(QBServiceConsts.LOAD_ATTACH_FILE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, file);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);
        String dialogId = (String) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG_ID);

        QBFile qbFile = privateChatHelper.loadAttachFile(file);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_ATTACH_FILE, qbFile);
        result.putString(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);

        return result;
    }
}