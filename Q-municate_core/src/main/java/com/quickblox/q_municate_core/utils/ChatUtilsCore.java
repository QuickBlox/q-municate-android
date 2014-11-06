package com.quickblox.q_municate_core.utils;

import com.quickblox.q_municate_core.models.User;

import java.util.ArrayList;
import java.util.List;

public class ChatUtilsCore {

    public static ArrayList<Integer> getFriendIdsList(List<User> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (User friend : friendList) {
            friendIdsList.add(friend.getUserId());
        }
        return friendIdsList;
    }
}
