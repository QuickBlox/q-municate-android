package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FinderUnknownFriends {

    private Context context;
    private QBDialog dialog;
    private List<QBDialog> dialogsList;
    private Set<Integer> loadIdsSet;
    private QBUser currentUser;
    private QBRestHelper restHelper;

    public FinderUnknownFriends(Context context, QBUser currentUser, List<QBDialog> dialogsList) {
        init(context, currentUser);
        this.dialogsList = dialogsList;
    }

    public FinderUnknownFriends(Context context, QBUser currentUser, QBDialog dialog) {
        init(context, currentUser);
        this.dialog = dialog;
    }

    private void init(Context context, QBUser currentUser) {
        this.context = context;
        this.currentUser = currentUser;
        loadIdsSet = new HashSet<Integer>();
        restHelper = new QBRestHelper(context);
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
        if (!loadIdsSet.isEmpty()) {
            loadUsers();
        }
    }

    private void loadUsers() {
        int oneElement = 1;
        try {
            if (loadIdsSet.size() == oneElement) {
                int userId = loadIdsSet.iterator().next();
                User user = restHelper.loadUser(userId);
                if (user != null) {
                    UsersDatabaseManager.saveUser(context, user);
                }
            } else {
                Collection<User> userCollection = restHelper.loadUsers(loadIdsSet);
                if (userCollection != null) {
                    UsersDatabaseManager.saveUsers(context, userCollection);
                }
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
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
    }
}