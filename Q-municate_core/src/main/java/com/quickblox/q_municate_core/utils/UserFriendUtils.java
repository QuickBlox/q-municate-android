package com.quickblox.q_municate_core.utils;

import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.q_municate_core.models.Friend;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_db.managers.DatabaseManager;
import com.quickblox.q_municate_db.models.Role;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserFriendUtils {

    public static List<User> getUsersFromFriends(List<com.quickblox.q_municate_db.models.Friend> friendList) {
        List<User> userList = new ArrayList<>(friendList.size());
        for (com.quickblox.q_municate_db.models.Friend friend : friendList) {
            userList.add(friend.getUser());
        }
        return userList;
    }

    public static User createLocalUser(QBUser qbUser, Role role) {
        User user = new User();
        user.setUserId(qbUser.getId());
        user.setFullName(qbUser.getFullName());
        user.setEmail(qbUser.getEmail());
        user.setPhone(qbUser.getPhone());
        user.setRole(role);

        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());

        if (userCustomData != null) {
            user.setAvatar(userCustomData.getAvatar_url());
            user.setStatus(userCustomData.getStatus());
        }

        return user;
    }

    public static User createLocalUser(QBUser qbUser) {
        Role role = DatabaseManager.getInstance().getRoleManager().getByRoleType(Role.Type.SIMPLE_ROLE);
        return createLocalUser(qbUser, role);
    }

    public static Friend createFriend(QBRosterEntry rosterEntry) {
        Friend friend = new Friend();
        friend.setUserId(rosterEntry.getUserId());
        friend.setRelationStatus(rosterEntry.getType().name());
        friend.setPendingStatus(isPendingFriend(rosterEntry));
        return friend;
    }

    public static boolean isPendingFriend(QBRosterEntry rosterEntry) {
        return RosterPacket.ItemStatus.subscribe.equals(rosterEntry.getStatus());
    }

    public static Friend createFriend(int userId) {
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setRelationStatus(RosterPacket.ItemType.none.name());
        return friend;
    }

    public static List<User> createUsersList(Collection<QBUser> usersList) {
        List<User> users = new ArrayList<User>();
        for (QBUser user : usersList) {
            users.add(createLocalUser(user));
        }
        return users;
    }

    public static List<Friend> createFriendsList(Collection<QBRosterEntry> rosterEntryCollection) {
        List<Friend> friendsList = new ArrayList<Friend>();
        for (QBRosterEntry rosterEntry : rosterEntryCollection) {
            friendsList.add(createFriend(rosterEntry));
        }
        return friendsList;
    }

    public static Map<Integer, User> createUserMap(List<QBUser> userList) {
        Map<Integer, User> userHashMap = new HashMap<Integer, User>();
        for (QBUser user : userList) {
            userHashMap.put(user.getId(), createLocalUser(user));
        }
        return userHashMap;
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

    public static Collection<Integer> getUserIdsFromRoster(Collection<QBRosterEntry> rosterEntryCollection) {
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBRosterEntry entry : rosterEntryCollection) {
            userIds.add(entry.getUserId());
        }
        return userIds;
    }
}