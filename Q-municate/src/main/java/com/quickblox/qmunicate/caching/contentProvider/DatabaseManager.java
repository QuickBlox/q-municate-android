package com.quickblox.qmunicate.caching.contentProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.quickblox.qmunicate.caching.contentProvider.tables.FriendTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    public static void saveFriends(Context context, List<Friend> friendsList) {
        for (Friend friend : friendsList) {
            saveFriend(context, friend);
        }
    }

    public static void saveFriend(Context context, Friend friend) {
        ContentValues values = new ContentValues();

        values.put(FriendTable.Cols.ID, friend.getEmail());
        values.put(FriendTable.Cols.FULLNAME, friend.getFullname());
        values.put(FriendTable.Cols.EMAIL, friend.getEmail());
        values.put(FriendTable.Cols.PHONE, friend.getPhone());
        values.put(FriendTable.Cols.FILE_ID, friend.getFileId());
        values.put(FriendTable.Cols.AVATAR_UID, friend.getAvatarUid());
        values.put(FriendTable.Cols.STATUS, friend.getStatus());
        values.put(FriendTable.Cols.LAST_REQUEST_AT, friend.getLastRequestAt());
        values.put(FriendTable.Cols.ONLINE, friend.getOnlineStatus());

        context.getContentResolver().insert(FriendTable.CONTENT_URI, values);
    }

    public static List<Friend> getFriendsList(Context context) {
        List<Friend> friendsList = new ArrayList<Friend>();
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                friendsList.add(getFriendFromCursor(cursor));
            } while (cursor.moveToNext());
        }

        cursor.close();

        return friendsList;
    }

    private static Friend getFriendFromCursor(Cursor cursor) {
        Friend friend = new Friend();

        friend.setId(cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ID)));
        friend.setFullname(cursor.getString(cursor.getColumnIndex(FriendTable.Cols.FULLNAME)));
        friend.setEmail(cursor.getString(cursor.getColumnIndex(FriendTable.Cols.EMAIL)));
        friend.setPhone(cursor.getString(cursor.getColumnIndex(FriendTable.Cols.PHONE)));
        friend.setFileId(cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.FILE_ID)));
        friend.setAvatarUid(cursor.getString(cursor.getColumnIndex(FriendTable.Cols.AVATAR_UID)));
        friend.setStatus(cursor.getString(cursor.getColumnIndex(FriendTable.Cols.STATUS)));
        friend.setLastRequestAt(cursor.getString(cursor.getColumnIndex(FriendTable.Cols.LAST_REQUEST_AT)));
        friend.setOnline(cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ONLINE)) > 0);

        return friend;
    }

    public static void deleteAllFriends(Context context) {
        context.getContentResolver().delete(FriendTable.CONTENT_URI, null, null);
    }
}