package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.qb.commands.QBLoadUserCommand;

import java.util.ArrayList;
import java.util.List;

public class FindUnknownFriendsTask extends AsyncTask {

    private Context context;

    public FindUnknownFriendsTask(Context context) {
        this.context = context;
    }

    @Override
    protected Uri doInBackground(Object[] params) {
        List<QBDialog> dialogsList = (List<QBDialog>) params[0];
        QBDialog dialog = (QBDialog) params[1];
        QBUser currentUser = AppSession.getSession().getUser();

        if (dialogsList != null) {
            findUserInDialogsList(dialogsList, currentUser.getId());
        } else {
            findUserInDialog(dialog, currentUser.getId());
        }

        return null;
    }

    private void findUserInDialogsList(List<QBDialog> dialogsList, int currentUserId) {
        for (QBDialog dialog : dialogsList) {
            findUserInDialog(dialog, currentUserId);
        }
    }

    private void findUserInDialog(QBDialog dialog, int currentUserId) {
        List<Integer> occupantsList = dialog.getOccupants();
        List<Integer> usersIdsList = new ArrayList<Integer>();
        for (int occupantId : occupantsList) {
            boolean isUserInBase = DatabaseManager.isUserInBase(context,
                    occupantId);
            if (!isUserInBase && currentUserId != occupantId) {
                usersIdsList.add(occupantId);
            }
        }
        if (!usersIdsList.isEmpty()) {
            int oneElement = 1;
            if (usersIdsList.size() == oneElement) {
                startLoadUser(usersIdsList.get(0));
            } else {
                startLoadUsers(usersIdsList);
            }
        }
    }

    private void startLoadUser(int userId) {
        QBLoadUserCommand.start(context, userId);
    }

    private void startLoadUsers(List<Integer> usersIdsList) {
        QBLoadUserCommand.start(context, usersIdsList);
    }
}