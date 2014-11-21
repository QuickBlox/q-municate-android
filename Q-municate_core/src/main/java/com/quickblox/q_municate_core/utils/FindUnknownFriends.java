package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.qb.commands.QBLoadUserCommand;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FindUnknownFriends {

    private Context context;
    private QBDialog dialog;
    private List<QBDialog> dialogsList;
    private Set<Integer> loadIdsSet;
    private QBUser currentUser;

    public FindUnknownFriends(Context context, QBUser currentUser, List<QBDialog> dialogsList) {
        init(context, currentUser);
        this.dialogsList = dialogsList;
    }

    public FindUnknownFriends(Context context, QBUser currentUser, QBDialog dialog) {
        init(context, currentUser);
        this.dialog = dialog;
    }

    private void init(Context context, QBUser currentUser) {
        this.context = context;
        this.currentUser = currentUser;
        loadIdsSet = new HashSet<Integer>();
    }

    public void find() {
        if (dialogsList != null) {
            findUserInDialogsList(dialogsList, currentUser.getId());
        } else {
            findUserInDialog(dialog, currentUser.getId());
        }
    }

    private void findUserInDialogsList(List<QBDialog> dialogsList, int currentUserId) {
        for (QBDialog dialog : dialogsList) {
            findUserInDialog(dialog, currentUserId);
        }
    }

    private void findUserInDialog(QBDialog dialog, int currentUserId) {
        List<Integer> occupantsList = dialog.getOccupants();
        for (int occupantId : occupantsList) {
            boolean isUserInBase = UsersDatabaseManager.isUserInBase(context, occupantId);
            if (!isUserInBase && currentUserId != occupantId) {
                loadIdsSet.add(occupantId);
            }
        }
        if (!loadIdsSet.isEmpty()) {
            int oneElement = 1;
            if (loadIdsSet.size() == oneElement) {
                startLoadUser(loadIdsSet.iterator().next());
            } else {
                startLoadUsers(new ArrayList<Integer>(loadIdsSet));
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