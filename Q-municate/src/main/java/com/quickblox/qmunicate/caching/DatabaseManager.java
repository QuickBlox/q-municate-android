package com.quickblox.qmunicate.caching;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;


import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.qmunicate.caching.tables.FriendTable;

import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.ChatMessage;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    //--------------------------------------- FriendTable -----------------------------------------------------

    public static void saveFriends(Context context, List<Friend> friendsList) {
        for (Friend friend : friendsList) {
            saveFriend(context, friend);
        }
    }

    public static void saveFriend(Context context, Friend friend) {
        ContentValues values = new ContentValues();

        values.put(FriendTable.Cols.ID, friend.getId());
        values.put(FriendTable.Cols.FULLNAME, friend.getFullname());
        values.put(FriendTable.Cols.EMAIL, friend.getEmail());
        values.put(FriendTable.Cols.PHONE, friend.getPhone());
        values.put(FriendTable.Cols.FILE_ID, friend.getFileId());
        values.put(FriendTable.Cols.AVATAR_UID, friend.getAvatarUid());
        values.put(FriendTable.Cols.STATUS, friend.getOnlineStatus());
        values.put(FriendTable.Cols.ONLINE, friend.isOnline());
        if (friend.getLastRequestAt() != null) {
            values.put(FriendTable.Cols.LAST_REQUEST_AT, DateUtils.dateToLong(friend.getLastRequestAt()));
        }

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

    public static Friend getFriendFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ID));
        String fullname = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.FULLNAME));
        String email = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.EMAIL));
        String phone = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.PHONE));
        int fileId = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.FILE_ID));
        String avatarUid = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.AVATAR_UID));
        long lastRequestAt = cursor.getLong(cursor.getColumnIndex(FriendTable.Cols.LAST_REQUEST_AT));

        Friend friend = new Friend(id, fullname, email, phone, fileId, avatarUid, DateUtils.longToDate(
                lastRequestAt));

        return friend;
    }

    public static boolean searchFriendInBase(Context context, int searchId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + searchId, null, null);
        return cursor.getCount() > Consts.ZERO_VALUE;
    }

    public static Cursor fetchFriendsByFullname(Context context, String inputText) {
        Cursor cursor = null;
        if (TextUtils.isEmpty(inputText)) {
            cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, null);
        } else {
            cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, FriendTable.Cols.FULLNAME + " like '%" + inputText + "%'", null, null);
        }
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public static Cursor getCursorFriendById(Context context, int friendId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, FriendTable.Cols.ID + " = " + friendId, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public static Cursor getAllFriends(Context context) {
        return context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, FriendTable.Cols.ID + " ORDER BY " + FriendTable.Cols.FULLNAME + " COLLATE NOCASE ASC");
    }

    public static void deleteAllFriends(Context context) {
        context.getContentResolver().delete(FriendTable.CONTENT_URI, null, null);
    }

    //--------------------------------------- ChatMessagesTable -----------------------------------------------------

    public static void savePrivateChatMessage(Context context, QBChatMessage message, int senderId, int chatId) {
        ContentValues values = new ContentValues();
        values.put(ChatMessagesTable.Cols.BODY, message.getBody());
        values.put(ChatMessagesTable.Cols.SENDER_ID, senderId);
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        // TODO INCOMING
        values.put(ChatMessagesTable.Cols.INCOMING, false);
        values.put(ChatMessagesTable.Cols.CHAT_ID, chatId);

        context.getContentResolver().insert(ChatMessagesTable.CONTENT_URI, values);
    }

    public static Cursor getAllPrivateChatMessagesBySenderId(Context context, int chatId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null, ChatMessagesTable.Cols.CHAT_ID + " = " + chatId, null, null);
    }

    public static Cursor getAllGroupChatMessagesByGroupId(Context context, String groupId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null, ChatMessagesTable.Cols.GROUP_ID + " = " + groupId, null, null);
    }
}