package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FinderUnknownUsers {

    private Context context;
    private QBDialog dialog;
    private List<QBDialog> dialogsList;
    private Set<Integer> loadIdsSet;
    private QBUser currentUser;
    private QBRestHelper restHelper;
    private DataManager dataManager;

    public FinderUnknownUsers(Context context, QBUser currentUser, List<QBDialog> dialogsList) {
        init(context, currentUser);
        this.dialogsList = dialogsList;
    }

    public FinderUnknownUsers(Context context, QBUser currentUser, QBDialog dialog) {
        init(context, currentUser);
        this.dialog = dialog;
    }

    private void init(Context context, QBUser currentUser) {
        this.context = context;
        this.currentUser = currentUser;
        loadIdsSet = new HashSet<Integer>();
        restHelper = new QBRestHelper(context);
        dataManager = DataManager.getInstance();
    }

    public void find() {
        if (dialogsList != null) {
            findUserInDialogsList(dialogsList);
        } else {
            findUserInDialog(dialog);
        }
    }

    private void findUserInDialogsList(List<QBDialog> dialogsList) {
        for (QBDialog dialog : dialogsList) {
            findUserInDialog(dialog);
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
                User user = QBRestHelper.loadUser(userId);
                dataManager.getUserDataManager().createOrUpdate(user);
            } else {
                Collection<User> userCollection = restHelper.loadUsers(loadIdsSet);
                if (userCollection != null) {
                    dataManager.getUserDataManager().createOrUpdateAll(userCollection);
                }
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
    }

    private void findUserInDialog(QBDialog dialog) {
        List<Integer> occupantsList = dialog.getOccupants();
        for (int occupantId : occupantsList) {
            boolean isUserInBase = dataManager.getUserDataManager().exists(occupantId);
            if (!isUserInBase && currentUser.getId() != occupantId) {
                loadIdsSet.add(occupantId);
            }
        }
    }
}