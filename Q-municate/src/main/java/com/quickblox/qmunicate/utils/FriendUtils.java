package com.quickblox.qmunicate.utils;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.model.Friend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendUtils {

    public static Friend createFriend(QBUser user) {
        Friend friend = new Friend();
        friend.setId(user.getId());
        friend.setFullname(user.getFullName());
        friend.setEmail(user.getEmail());
        friend.setPhone(user.getPhone());
        friend.setFileId(user.getFileId());
        friend.setAvatarUrl(user.getWebsite());
        return friend;
    }

    public static List<Friend> createFriendList(List<QBUser> userList) {
        List<Friend> friends = new ArrayList<Friend>();
        for (QBUser user : userList) {
            friends.add(createFriend(user));
        }
        return friends;
    }

    public static Map<Integer, Friend> createFriendMap(List<QBUser> userList) {
        Map<Integer, Friend> friendMap = new HashMap<Integer, Friend>();
        for (QBUser user : userList) {
            friendMap.put(user.getId(), createFriend(user));
        }
        return friendMap;
    }
}
