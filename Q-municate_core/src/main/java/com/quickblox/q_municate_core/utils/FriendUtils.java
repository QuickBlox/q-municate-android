package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.database.MatrixCursor;

import com.quickblox.chat.model.QBRosterEntry;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.db.tables.UserTable;
import com.quickblox.q_municate_core.models.Friend;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.packet.RosterPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendUtils {

    public static User createUser(QBUser qbUser) {
        qbUser.setCustomDataClass(UserCustomData.class);
        User user = new User();
        user.setUserId(qbUser.getId());
        user.setFullName(qbUser.getFullName());
        user.setEmail(qbUser.getEmail());
        user.setPhone(qbUser.getPhone());

        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());

        if (userCustomData != null) {
            user.setAvatarUrl(userCustomData.getAvatar_url());
            user.setStatus(userCustomData.getStatus());
        }

        return user;
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
            users.add(createUser(user));
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

    public static List<Integer> getUserIdsFromRoster(Collection<QBRosterEntry> rosterEntryCollection) {
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBRosterEntry entry : rosterEntryCollection) {
            userIds.add(entry.getUserId());
        }
        return userIds;
    }

    public static MatrixCursor createSearchResultCursor(Context context, Collection<User> usersList) {
        MatrixCursor usersCursor = new MatrixCursor(
                new String[]{UserTable.Cols.ID, UserTable.Cols.USER_ID, UserTable.Cols.FULL_NAME, UserTable.Cols.EMAIL, UserTable.Cols.PHONE, UserTable.Cols.AVATAR_URL, UserTable.Cols.STATUS, UserTable.Cols.IS_ONLINE, FriendTable.Cols.RELATION_STATUS_ID, FriendTable.Cols.IS_PENDING_STATUS});

        List<User> friendsList = UsersDatabaseManager.getAllFriendsList(context);

        for (User user : usersList) {
            if (!friendsList.contains(user)) {
                usersCursor.addRow(new String[]{user.getUserId() + ConstsCore.EMPTY_STRING, user
                        .getUserId() + ConstsCore.EMPTY_STRING, user.getFullName(), user.getEmail(), user
                        .getPhone(), user.getAvatarUrl(), user
                        .getStatus(), ConstsCore.ZERO_INT_VALUE + ConstsCore.EMPTY_STRING, QBFriendListHelper.VALUE_RELATION_STATUS_ALL_USERS + ConstsCore.EMPTY_STRING, ConstsCore.ZERO_INT_VALUE + ConstsCore.EMPTY_STRING});
            }
        }

        return usersCursor;
    }
}