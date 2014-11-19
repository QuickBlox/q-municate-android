package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.utils.FriendUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.Collection;

public class QBRestHelper extends BaseHelper {

    public QBRestHelper(Context context) {
        super(context);
    }

    public User loadUser(int userId) throws QBResponseException {
        QBUser user = QBUsers.getUser(userId);
        User resultUser = null;

        if (user != null) {
            resultUser = FriendUtils.createUser(user);
            UsersDatabaseManager.saveUser(context, resultUser);
        }

        return resultUser;
    }

    public Collection<User> loadUsers(Collection<Integer> usersIdsList) throws QBResponseException {
        Collection<QBUser> usersList = (Collection<QBUser>) QBUsers.getUsersByIDs(usersIdsList, null);

        Collection<User> usersListResult = FriendUtils.createUsersList(usersList);

        if (usersList != null) {
            UsersDatabaseManager.saveUsers(context, usersListResult);
        }

        return usersListResult;
    }
}