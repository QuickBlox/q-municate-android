package com.quickblox.q_municate_core.db.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.db.tables.FriendsRelationTable;
import com.quickblox.q_municate_core.db.tables.UserTable;
import com.quickblox.q_municate_core.models.Friend;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersDatabaseManager {

    private static String USER_FRIEND_RELATION_KEY = UserTable.TABLE_NAME + "." + UserTable.Cols.USER_ID + " = " + FriendTable.TABLE_NAME + "." + FriendTable.Cols.USER_ID;
    private static Map<String, Integer> relationStatusesMap;

    public static void savePeople(Context context, List<User> usersList, List<Friend> friendsList) {
        for (User user : usersList) {
            saveUser(context, user);
        }
        for (Friend friend : friendsList) {
            saveFriend(context, friend);
        }
    }

    public static void saveUsers(Context context, Collection<User> usersList) {
        for (User user : usersList) {
            saveUser(context, user);
        }
    }

    public static void saveUser(Context context, User user) {
        ContentValues values = getContentValuesUserTable(user);

        String condition = UserTable.Cols.USER_ID + "='" + user.getUserId() + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(UserTable.CONTENT_URI, null, condition, null, null);

        if (cursor != null && cursor.getCount() > ConstsCore.ZERO_INT_VALUE) {
            resolver.update(UserTable.CONTENT_URI, values, condition, null);
        } else {
            resolver.insert(UserTable.CONTENT_URI, values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    public static void saveFriend(Context context, Friend friend) {
        int relationStatusId = getRelationStatusIdByName(context, friend.getRelationStatus());

        friend.setRelationStatusId(relationStatusId);

        ContentValues values = getContentValuesFriendTable(friend);

        String condition = FriendTable.Cols.USER_ID + "='" + friend.getUserId() + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(FriendTable.CONTENT_URI, null, condition, null, null);

        if (cursor != null && cursor.getCount() > ConstsCore.ZERO_INT_VALUE) {
            resolver.update(FriendTable.CONTENT_URI, values, condition, null);
        } else {
            resolver.insert(FriendTable.CONTENT_URI, values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    private static ContentValues getContentValuesFriendTable(Friend friend) {
        ContentValues values = new ContentValues();

        values.put(FriendTable.Cols.USER_ID, friend.getUserId());
        values.put(FriendTable.Cols.RELATION_STATUS_ID, friend.getRelationStatusId());
        values.put(FriendTable.Cols.IS_PENDING_STATUS, friend.isPendingStatus());
        values.put(FriendTable.Cols.IS_NEW_FRIEND_STATUS, friend.isNewFriendStatus());

        return values;
    }

    public static int getRelationStatusIdByName(Context context, String relationStatus) {
        int relationStatusId = ConstsCore.ZERO_INT_VALUE;

        Cursor cursor = context.getContentResolver().query(FriendsRelationTable.CONTENT_URI, null,
                FriendsRelationTable.Cols.RELATION_STATUS + " = '" + relationStatus + "'", null, null);

        if (cursor != null && cursor.moveToFirst()) {
            relationStatusId = cursor.getInt(cursor.getColumnIndex(
                    FriendsRelationTable.Cols.RELATION_STATUS_ID));
        }

        if (cursor != null) {
            cursor.close();
        }

        return relationStatusId;
    }

    private static Map<String, Integer> getRelationStatusesMap(Context context) {
        Map<String, Integer> relationStatusesMap = new HashMap<String, Integer>();

        Cursor cursor = context.getContentResolver().query(FriendsRelationTable.CONTENT_URI, null, null, null,
                null);

        if (cursor != null) {
            cursor.moveToFirst();

            while (!cursor.isLast()) {
                relationStatusesMap.put(cursor.getString(cursor.getColumnIndex(
                        FriendsRelationTable.Cols.RELATION_STATUS)), cursor.getInt(cursor.getColumnIndex(
                        FriendsRelationTable.Cols.RELATION_STATUS_ID)));
                cursor.moveToNext();
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return relationStatusesMap;
    }

    public static String getRelationStatusNameById(Context context, int relationStatusId) {
        String relationStatus = null;

        Cursor cursor = context.getContentResolver().query(FriendsRelationTable.CONTENT_URI, null,
                FriendsRelationTable.Cols.RELATION_STATUS_ID + " = " + relationStatusId, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            relationStatus = cursor.getString(cursor.getColumnIndex(
                    FriendsRelationTable.Cols.RELATION_STATUS));
        }

        if (cursor != null) {
            cursor.close();
        }

        return relationStatus;
    }

    public static int getRelationStatusIdByUserId(Context context, int userId) {
        int relationStatusId = ConstsCore.ZERO_INT_VALUE;

        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.USER_ID + " = " + userId, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            relationStatusId = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.RELATION_STATUS_ID));
        }

        if (cursor != null) {
            cursor.close();
        }

        return relationStatusId;
    }

    public static boolean isUserInBase(Context context, int searchId) {
        String condition = FriendTable.Cols.USER_ID + " = " + searchId;

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(UserTable.CONTENT_URI, null, condition, null, null);

        boolean isUserInBase = cursor.getCount() > ConstsCore.ZERO_INT_VALUE;

        cursor.close();

        return isUserInBase;
    }

    public static boolean isFriendInBaseWithPending(Context context, int searchId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.USER_ID + " = " + searchId, null, null);
        boolean isFriendInBase = cursor.getCount() > ConstsCore.ZERO_INT_VALUE;

        cursor.close();

        return isFriendInBase;
    }

    public static boolean isFriendInBase(Context context, int searchId) {
        int relationStatusNoneId = getRelationStatusIdByName(context,
                QBFriendListHelper.RELATION_STATUS_NONE);

        String condition = FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " != " + relationStatusNoneId +
                " AND (" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.USER_ID + " = " + searchId + ")";

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(UserTable.USER_FRIEND_CONTENT_URI, null,
                USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, null);

        boolean isFriendInBase = cursor.getCount() > ConstsCore.ZERO_INT_VALUE;

        cursor.close();

        return isFriendInBase;
    }

    public static boolean isFriendWithStatusNew(Context context, int searchId) {
        String condition = FriendTable.Cols.USER_ID + " = " + searchId + " AND " + FriendTable.Cols.IS_NEW_FRIEND_STATUS + " = 1";

        ContentResolver resolver = context.getContentResolver();

        Cursor cursor = resolver.query(FriendTable.CONTENT_URI, null, condition, null, null);

        boolean isFriend = cursor.getCount() > ConstsCore.ZERO_INT_VALUE;

        cursor.close();

        return isFriend;
    }

    public static Cursor getAllFriendsWithPending(Context context) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);
        int relationStatusNoneId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_NONE);

        String condition = "(" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ") " + " OR (" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " = " + relationStatusNoneId + " AND " + FriendTable.TABLE_NAME + "." + FriendTable.Cols.IS_PENDING_STATUS + " = 1" + "))";

        String sortOrder = UserTable.TABLE_NAME + "." + UserTable.Cols.ID + " ORDER BY " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " COLLATE NOCASE ASC";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(UserTable.USER_FRIEND_CONTENT_URI, null,
                USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, sortOrder);

        return cursor;
    }

    public static Cursor getFriendsByFullNameWithPending(Context context, String fullName) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);
        int relationStatusNoneId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_NONE);

        String condition = "(" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ") " + " OR (" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " = " + relationStatusNoneId + " AND " + FriendTable.TABLE_NAME + "." + FriendTable.Cols.IS_PENDING_STATUS + " = 1" + ")) AND " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " like '%" + fullName + "%'";

        String sorting = UserTable.TABLE_NAME + "." + UserTable.Cols.ID + " ORDER BY " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " COLLATE NOCASE ASC";

        Cursor cursor;

        if (TextUtils.isEmpty(fullName)) {
            cursor = getAllFriendsWithPending(context);
        } else {
            cursor = context.getContentResolver().query(UserTable.USER_FRIEND_CONTENT_URI, null,
                    USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, sorting);
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public static Cursor getFriendsByFullName(Context context, String fullName) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);

        String condition = "(" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ")) AND " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " like '%" + fullName + "%'";

        String sorting = UserTable.TABLE_NAME + "." + UserTable.Cols.ID + " ORDER BY " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " COLLATE NOCASE ASC";

        Cursor cursor;

        if (TextUtils.isEmpty(fullName)) {
            cursor = getAllFriends(context);
        } else {
            cursor = context.getContentResolver().query(UserTable.USER_FRIEND_CONTENT_URI, null,
                    USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, sorting);
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public static Cursor getAllFriends(Context context) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);

        String condition = FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ")";

        String sortOrder = UserTable.TABLE_NAME + "." + UserTable.Cols.ID + " ORDER BY " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " COLLATE NOCASE ASC";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(UserTable.USER_FRIEND_CONTENT_URI, null,
                USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, sortOrder);

        return cursor;
    }

    public static int getAllFriendsCount(Context context) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);

        String condition = FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ")";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(UserTable.USER_FRIEND_CONTENT_URI, new String[]{"count(*)"},
                USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, null);

        return processResultCount(cursor);
    }

    public static int getAllFriendsCountByFullNameWithPending(Context context, String fullName) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);
        int relationStatusNoneId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_NONE);

        String condition = "(" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ") " + " OR (" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " = " + relationStatusNoneId + " AND " + FriendTable.TABLE_NAME + "." + FriendTable.Cols.IS_PENDING_STATUS + " = 1" + ")) AND " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " like '%" + fullName + "%'";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(UserTable.USER_FRIEND_CONTENT_URI, new String[]{"count(*)"},
                USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, null);

        return processResultCount(cursor);
    }

    public static int getAllFriendsCountWithPending(Context context) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);
        int relationStatusNoneId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_NONE);

        String condition = FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ") " + " OR (" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID + " = " + relationStatusNoneId + " AND " + FriendTable.TABLE_NAME + "." + FriendTable.Cols.IS_PENDING_STATUS + " = 1" + ")";

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(UserTable.USER_FRIEND_CONTENT_URI, new String[]{"count(*)"},
                USER_FRIEND_RELATION_KEY + " AND (" + condition + ")", null, null);

        return processResultCount(cursor);
    }

    private static int processResultCount(Cursor cursor) {
        if (cursor.getCount() == ConstsCore.ZERO_INT_VALUE) {
            cursor.close();
            return ConstsCore.ZERO_INT_VALUE;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(ConstsCore.ZERO_INT_VALUE);
            cursor.close();
            return result;
        }
    }

    public static List<User> getAllFriendsList(Context context) {
        List<User> friendList = new ArrayList<User>();
        Cursor cursor = getAllFriendsWithPending(context);

        while (cursor.moveToNext()) {
            friendList.add(getUserFromCursor(cursor));
        }

        cursor.close();

        return friendList;
    }

    public static Cursor getFriendsFilteredByIds(Context context, List<Integer> friendIdsList) {
        String selection = prepareFriendsFilter(context, friendIdsList);
        String sortOrder = UserTable.TABLE_NAME + "." + UserTable.Cols.ID + " ORDER BY " + UserTable.TABLE_NAME + "." + UserTable.Cols.FULL_NAME + " COLLATE NOCASE ASC";

        return context.getContentResolver().query(UserTable.USER_FRIEND_CONTENT_URI, null,
                USER_FRIEND_RELATION_KEY + " AND (" + selection + ")", null, sortOrder);
    }

    private static String prepareFriendsFilter(Context context, List<Integer> friendIdsList) {
        if (relationStatusesMap == null) {
            relationStatusesMap = getRelationStatusesMap(context);
        }

        int relationStatusFromId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_FROM);
        int relationStatusToId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_TO);
        int relationStatusBothId = relationStatusesMap.get(QBFriendListHelper.RELATION_STATUS_BOTH);

        String condition = String.format("('%s')", TextUtils.join("','", friendIdsList));
        String conditionResult = "(" + FriendTable.TABLE_NAME + "." + FriendTable.Cols.RELATION_STATUS_ID +
                " IN (" + relationStatusFromId + "," + relationStatusToId + "," + relationStatusBothId + ")) AND " + UserTable.TABLE_NAME + "." + UserTable.Cols.USER_ID + " NOT IN " + condition;

        return conditionResult;
    }

    public static void deleteAllFriends(Context context) {
        context.getContentResolver().delete(FriendTable.CONTENT_URI, null, null);
    }

    public static void deleteAllUsers(Context context) {
        context.getContentResolver().delete(UserTable.CONTENT_URI, null, null);
    }

    public static User getUserById(Context context, long userId) {
        Cursor cursor = context.getContentResolver().query(UserTable.CONTENT_URI, null,
                UserTable.Cols.USER_ID + " = " + userId, null, null);
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = getUserFromCursor(cursor);
            cursor.close();
        }
        return user;
    }

    public static Friend getFriendById(Context context, long friendId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.USER_ID + " = " + friendId, null, null);
        Friend friend = null;
        if (cursor != null && cursor.moveToFirst()) {
            friend = getFriendFromCursor(cursor);
            String relationStatus = getRelationStatusNameById(context, friend.getRelationStatusId());
            friend.setRelationStatus(relationStatus);
            cursor.close();
        }
        return friend;
    }

    public static Cursor getFriendCursorById(Context context, int friendId) {
        Cursor cursor = context.getContentResolver().query(UserTable.CONTENT_URI, null,
                UserTable.Cols.USER_ID + " = " + friendId, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public static User getUserFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(UserTable.Cols.USER_ID));
        String fullName = cursor.getString(cursor.getColumnIndex(UserTable.Cols.FULL_NAME));
        String email = cursor.getString(cursor.getColumnIndex(UserTable.Cols.EMAIL));
        String phone = cursor.getString(cursor.getColumnIndex(UserTable.Cols.PHONE));
        String avatarUid = cursor.getString(cursor.getColumnIndex(UserTable.Cols.AVATAR_URL));
        String status = cursor.getString(cursor.getColumnIndex(UserTable.Cols.STATUS));
        boolean online = cursor.getInt(cursor.getColumnIndex(UserTable.Cols.IS_ONLINE)) > 0;

        User user = new User(id, fullName, email, phone, avatarUid);
        user.setStatus(status);
        user.setOnline(online);

        return user;
    }

    public static Friend getFriendFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.USER_ID));
        int relationStatusId = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.RELATION_STATUS_ID));
        boolean isPendingStatus = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.IS_PENDING_STATUS)) > 0;
        boolean isNewFriendStatus = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.IS_NEW_FRIEND_STATUS)) > 0;

        Friend friend = new Friend(id);
        friend.setRelationStatusId(relationStatusId);
        friend.setPendingStatus(isPendingStatus);
        friend.setNewFriendStatus(isNewFriendStatus);

        return friend;
    }

    private static ContentValues getContentValuesUserTable(User user) {
        ContentValues values = new ContentValues();
        values.put(UserTable.Cols.USER_ID, user.getUserId());
        values.put(UserTable.Cols.FULL_NAME, user.getFullName());
        values.put(UserTable.Cols.EMAIL, user.getEmail());
        values.put(UserTable.Cols.PHONE, user.getPhone());
        values.put(UserTable.Cols.AVATAR_URL, user.getAvatarUrl());
        values.put(UserTable.Cols.STATUS, user.getStatus());
        values.put(UserTable.Cols.IS_ONLINE, user.isOnline());
        return values;
    }
}