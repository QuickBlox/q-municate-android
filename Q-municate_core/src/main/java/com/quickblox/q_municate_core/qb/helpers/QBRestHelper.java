package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Collection;

public class QBRestHelper extends BaseHelper {

    public QBRestHelper(Context context) {
        super(context);
    }

    public User loadUser(int userId) {
        User resultUser = null;

        try {
            QBUser user = QBUsers.getUser(userId);
            resultUser = FriendUtils.createUser(user);
        } catch (QBResponseException e) {
            // user not found
        }

        return resultUser;
    }

    public Collection<User> loadUsers(Collection<Integer> usersIdsList) throws QBResponseException {
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(ConstsCore.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(ConstsCore.FL_FRIENDS_PER_PAGE);
        Collection<QBUser> usersList = QBUsers.getUsersByIDs(usersIdsList, requestBuilder, new Bundle());
        Collection<User> usersListResult = FriendUtils.createUsersList(usersList);
        return usersListResult;
    }
}