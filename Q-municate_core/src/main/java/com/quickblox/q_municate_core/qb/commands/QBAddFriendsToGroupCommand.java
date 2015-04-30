package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import java.util.ArrayList;

public class QBAddFriendsToGroupCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBAddFriendsToGroupCommand(Context context, QBMultiChatHelper chatHelper, String successAction,
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
    public Bundle perform(Bundle extras) throws QBResponseException {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        ArrayList<Integer> friendIdsList = (ArrayList<Integer>) extras.getSerializable(
                QBServiceConsts.EXTRA_FRIENDS);

        QBDialog dialog = multiChatHelper.addUsersToDialog(dialogId, friendIdsList);

        if (dialog != null) {
            ChatDatabaseManager.saveDialog(context, dialog);
        }

        Bundle returnedBundle = new Bundle();
        returnedBundle.putSerializable(QBServiceConsts.EXTRA_DIALOG, dialog);
        return returnedBundle;
    }
}