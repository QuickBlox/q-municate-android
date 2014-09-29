package com.quickblox.q_municate.utils;

import android.content.Context;
import android.database.MatrixCursor;

import com.quickblox.module.chat.model.QBRosterEntry;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.db.tables.FriendTable;
import com.quickblox.q_municate.db.tables.UserTable;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.helpers.QBFriendListHelper;

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

    public static Friend createFriend(int userId, boolean requestedFriend) {
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setRelationStatus(RosterPacket.ItemType.none.name());
        friend.setRequestedFriend(requestedFriend);
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

    public static Map<Integer, User> createUserMap(List<QBUser> userList) {
        Map<Integer, User> userHashMap = new HashMap<Integer, User>();
        for (QBUser user : userList) {
            userHashMap.put(user.getId(), createUser(user));
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

    public static List<Integer> getUserIdsFromRoster(Collection<QBRosterEntry> rosterEntryCollection){
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBRosterEntry entry : rosterEntryCollection) {
            userIds.add(entry.getUserId());
        }
        return userIds;
    }

    public static MatrixCursor createSearchResultCursor(Context context, List<User> usersList) {
        MatrixCursor usersCursor = new MatrixCursor(
                new String[]{UserTable.Cols.ID, UserTable.Cols.USER_ID, UserTable.Cols.FULL_NAME, UserTable.Cols.EMAIL, UserTable.Cols.PHONE, UserTable.Cols.AVATAR_URL, UserTable.Cols.STATUS, UserTable.Cols.IS_ONLINE, FriendTable.Cols.RELATION_STATUS_ID, FriendTable.Cols.IS_STATUS_ASK, FriendTable.Cols.IS_REQUESTED_FRIEND});

        List<User> friendsList = DatabaseManager.getAllFriendsList(context);

        for (User user : usersList) {
            if (!friendsList.contains(user)) {
                usersCursor.addRow(new String[]{user.getUserId() + Consts.EMPTY_STRING, user
                        .getUserId() + Consts.EMPTY_STRING, user.getFullName(), user.getEmail(), user
                        .getPhone(), user.getAvatarUrl(), user
                        .getStatus(), Consts.ZERO_INT_VALUE + Consts.EMPTY_STRING, QBFriendListHelper.VALUE_RELATION_STATUS_ALL_USERS + Consts.EMPTY_STRING, Consts.ZERO_INT_VALUE + Consts.EMPTY_STRING, Consts.ZERO_INT_VALUE + Consts.EMPTY_STRING});
            }
        }

        return usersCursor;
    }
}