package com.quickblox.q_municate.utils;

import com.quickblox.module.chat.QBRosterEntry;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.User;

import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendUtils {

    public static User createUser(QBUser qbUser) {
        User user = new User();
        user.setUserId(qbUser.getId());
        user.setFullName(qbUser.getFullName());
        user.setEmail(qbUser.getEmail());
        user.setPhone(qbUser.getPhone());
        user.setAvatarUrl(qbUser.getWebsite());
        return user;
    }

    public static Friend createFriend(QBRosterEntry rosterEntry) {
        Friend friend = new Friend();
        friend.setUserId(rosterEntry.getUserId());
        friend.setRelationStatus(rosterEntry.getType().name());
        if(RosterPacket.ItemStatus.subscribe.equals(rosterEntry.getStatus())) {
            friend.setAskStatus(true);
        }
        return friend;
    }

    public static Friend createFriend(int userId) {
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setRelationStatus(RosterPacket.ItemType.none.name());
        friend.setRequestedFriend(true);
        return friend;
    }

    public static List<User> createUsersList(List<QBUser> userList) {
        List<User> friends = new ArrayList<User>();
        for (QBUser user : userList) {
            friends.add(createUser(user));
        }
        return friends;
    }

    public static List<Friend> createFriendsList(Collection<QBRosterEntry> rosterEntryCollection) {
        List<Friend> friendsList = new ArrayList<Friend>();
        for (QBRosterEntry rosterEntry : rosterEntryCollection) {
            friendsList.add(createFriend(rosterEntry));
        }
        return friendsList;
    }

    public static Map<Integer, User> createFriendMap(List<QBUser> userList) {
        Map<Integer, User> friendMap = new HashMap<Integer, User>();
        for (QBUser user : userList) {
            friendMap.put(user.getId(), createUser(user));
        }
        return friendMap;
    }

    public static ArrayList<Integer> getFriendIdsList(List<QBUser> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (QBUser friend : friendList) {
            friendIdsList.add(friend.getId());
        }
        return friendIdsList;
    }

    public static ArrayList<Integer> getFriendIds(List<User> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (User friend : friendList) {
            friendIdsList.add(friend.getUserId());
        }
        return friendIdsList;
    }

    public static List<Integer> getUserIdsFromRoster(Collection<QBRosterEntry> rosterEntryCollection){
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBRosterEntry entry : rosterEntryCollection) {
            userIds.add(entry.getUserId());
        }
        return userIds;
    }
}