package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;

import java.io.File;

public class QBUpdateGroupDialogCommand extends ServiceCommand {

    private QBGroupChatHelper multiChatHelper;

    public QBUpdateGroupDialogCommand(Context context, QBGroupChatHelper multiChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, QBDialog dialog, File file) {
        Intent intent = new Intent(QBServiceConsts.UPDATE_GROUP_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG, dialog);
        intent.putExtra(QBServiceConsts.EXTRA_FILE, file);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBDialog dialog = (QBDialog) extras.getSerializable(QBServiceConsts.EXTRA_DIALOG);
        File file = (File) extras.getSerializable(QBServiceConsts.EXTRA_FILE);

        if(file == null) {
            dialog = multiChatHelper.updateDialog(dialog);
        } else {
            dialog = multiChatHelper.updateDialog(dialog, file);
        }

        if (dialog != null) {
            DataManager.getInstance().getDialogDataManager().update(ChatUtils.createLocalDialog(dialog));
        }

        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_DIALOG, dialog);

        return bundle;
    }
}