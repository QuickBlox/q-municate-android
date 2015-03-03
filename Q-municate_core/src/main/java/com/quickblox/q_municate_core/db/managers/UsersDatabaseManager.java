package com.quickblox.q_municate_core.db.managers;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.db.tables.FriendsRelationTable;
import com.quickblox.q_municate_core.db.tables.UserTable;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersDatabaseManager {

    private static String USER_FRIEND_RELATION_KEY = UserTable.TABLE_NAME + "." + UserTable.Cols.USER_ID + " = " + FriendTable.TABLE_NAME + "." + FriendTable.Cols.USER_ID;
    private static Map<String, Integer> relationStatusesMap;

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

        User user = new User();
        //        User user = new User(id, fullName, email, phone, avatarUid);
        user.setStatus(status);
        user.setOnline(online);

        return user;
    }
}