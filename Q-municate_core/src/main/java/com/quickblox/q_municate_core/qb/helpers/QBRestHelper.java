package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Collection;

public class QBRestHelper extends BaseHelper {

    public QBRestHelper(Context context) {
        super(context);
    }

    public static User loadUser(int userId) {
        User resultUser;

        try {
            QBUser user = QBUsers.getUser(userId);
            resultUser = UserFriendUtils.createLocalUser(user);
        } catch (QBResponseException e) {
            // user not found
            resultUser = UserFriendUtils.createDeletedUser(userId);
        }

        return resultUser;
    }

    public static User loadAndSaveUser(int userId) {
        User resultUser = null;

        try {
            QBUser user = QBUsers.getUser(userId);
            resultUser = UserFriendUtils.createLocalUser(user);
        } catch (QBResponseException e) {
            // user not found
            resultUser = UserFriendUtils.createDeletedUser(userId);
        }

        DataManager.getInstance().getUserDataManager().createOrUpdate(resultUser, true);

        return resultUser;
    }

    public Collection<User> loadUsers(Collection<Integer> usersIdsList) throws QBResponseException {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(ConstsCore.USERS_PAGE_NUM);
        requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);
        Collection<QBUser> usersList = QBUsers.getUsersByIDs(usersIdsList, requestBuilder, new Bundle());
        Collection<User> usersListResult = UserFriendUtils.createUsersList(usersList);
        return usersListResult;
    }
}