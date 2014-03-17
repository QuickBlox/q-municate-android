package com.quickblox.qmunicate.qb.command;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.qmunicate.core.command.BaseCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

public class QBGetFileCommand extends BaseCommand {

    private static final String TAG = QBGetFileCommand.class.getSimpleName();

    public static void start(Context context, int fileId) {
        Intent intent = new Intent(QBServiceConsts.GET_FILE_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FILE_ID, fileId);
        context.startService(intent);
    }

    public QBGetFileCommand(Context context, String resultAction) {
        super(context, resultAction);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        Integer fileId = extras.getInt(QBServiceConsts.EXTRA_FILE_ID);

        QBFile qbFile = new QBFile(fileId);
        qbFile = QBContent.getFile(qbFile);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FILE, qbFile);

        return result;
    }
}