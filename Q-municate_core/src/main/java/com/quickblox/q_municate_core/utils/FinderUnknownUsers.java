package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.chat.model.QBChatDialog ;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FinderUnknownUsers {

    private Context context;
    private QBChatDialog  dialog;
    private List<QBChatDialog > dialogsList;
    private Set<Integer> loadIdsSet;
    private QBUser currentUser;
    private QBRestHelper restHelper;
    private DataManager dataManager;

    public FinderUnknownUsers(Context context, QBUser currentUser, List<QBChatDialog > dialogsList) {
        init(context, currentUser);
        this.dialogsList = dialogsList;
    }

    public FinderUnknownUsers(Context context, QBUser currentUser, QBChatDialog  dialog) {
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

    private void findUserInDialogsList(List<QBChatDialog > dialogsList) {
        for (QBChatDialog  dialog : dialogsList) {
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
                QMUser user = QMUserService.getInstance().getUserSync(userId, true);
            } else {
                Collection<QMUser> userCollection = loadUsers(loadIdsSet);
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
    }

    private List<QMUser> loadUsers(Set<Integer> loadIdsSet) throws QBResponseException {
        return QMUserService.getInstance().getUsersByIDsSync(loadIdsSet, null);
    }

    private void findUserInDialog(QBChatDialog  dialog) {
        List<Integer> occupantsList = dialog.getOccupants();
        for (int occupantId : occupantsList) {
            boolean isUserInBase = QMUserService.getInstance().getUserCache().exists((long)occupantId);
            if (!isUserInBase && currentUser.getId().intValue() != occupantId) {
                loadIdsSet.add(occupantId);
            }
        }
    }
}