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
import com.quickblox.q_municate_core.utils.DbUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;

import java.util.ArrayList;

public class QBAddFriendsToGroupCommand extends ServiceCommand {

    private QBGroupChatHelper multiChatHelper;

    public QBAddFriendsToGroupCommand(Context context, QBGroupChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = chatHelper;
    }

    public static void start(Context context, String dialogId, ArrayList<Integer> friendIdsList) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIENDS_TO_GROUP_ACTION, null, context,
                QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendIdsList);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        ArrayList<Integer> friendIdsList = (ArrayList<Integer>) extras.getSerializable(QBServiceConsts.EXTRA_FRIENDS);

        QBDialog qbDialog = multiChatHelper.addUsersToDialog(dialogId, friendIdsList);

        if (qbDialog != null) {
            Dialog dialog = ChatUtils.createLocalDialog(qbDialog);
            DataManager.getInstance().getDialogDataManager().update(dialog);
            DbUtils.saveDialogsOccupants(DataManager.getInstance(), qbDialog, true);
        }

        Bundle returnedBundle = new Bundle();
        returnedBundle.putSerializable(QBServiceConsts.EXTRA_DIALOG, qbDialog);

        return returnedBundle;
    }
}