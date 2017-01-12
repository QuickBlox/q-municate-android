package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBGetFileCommand extends ServiceCommand {

    private static final String TAG = QBGetFileCommand.class.getSimpleName();

    public QBGetFileCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, int fileId) {
        Intent intent = new Intent(QBServiceConsts.GET_FILE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FILE_ID, fileId);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        Integer fileId = extras.getInt(QBServiceConsts.EXTRA_FILE_ID);

        QBFile qbFile = QBContent.getFile(fileId).perform();

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FILE, qbFile);

        return result;
    }
}