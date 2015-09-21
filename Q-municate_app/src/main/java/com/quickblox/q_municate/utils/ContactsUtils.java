package com.quickblox.q_municate.utils;

import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;

import java.util.ArrayList;
import java.util.List;

public class ContactsUtils {

    public static List<User> createContactsList(DataManager dataManager) {
        List<User> contactsList = new ArrayList<>();

        List<Friend> friendsList = dataManager.getFriendDataManager().getAll();
        int countFriends = friendsList.size();
        if (countFriends > 0) {
            contactsList.addAll(UserFriendUtils.getUsersFromFriends(friendsList));
        }

        List<UserRequest> userRequestList = dataManager.getUserRequestDataManager().getAll();
        int countUserRequests = userRequestList.size();
        if (countUserRequests > 0) {
            List<User> userList = UserFriendUtils.getUsersFromUserRequest(userRequestList);
            if (!userList.isEmpty()) {
                contactsList.addAll(userList);
            }
        }

        return contactsList;
    }
}