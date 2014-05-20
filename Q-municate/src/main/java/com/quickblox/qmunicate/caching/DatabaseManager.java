package com.quickblox.qmunicate.caching;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.utils.Consts;

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
        ContentValues values = getContentValues(friend);
        context.getContentResolver().insert(FriendTable.CONTENT_URI, values);
    }

    private static ContentValues getContentValues(Friend friend) {
        ContentValues values = new ContentValues();
        values.put(FriendTable.Cols.ID, friend.getId());
        values.put(FriendTable.Cols.FULLNAME, friend.getFullname());
        values.put(FriendTable.Cols.EMAIL, friend.getEmail());
        values.put(FriendTable.Cols.PHONE, friend.getPhone());
        values.put(FriendTable.Cols.FILE_ID, friend.getFileId());
        values.put(FriendTable.Cols.AVATAR_UID, friend.getAvatarUid());
        values.put(FriendTable.Cols.STATUS, friend.getStatus());
        values.put(FriendTable.Cols.ONLINE, friend.isOnline());
        values.put(FriendTable.Cols.TYPE, friend.getType().name());
        return values;
    }

    public static List<Friend> getFriends(Context context) {
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

    public static Friend getFriend(Context context, int friendId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + friendId, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            return getFriendFromCursor(cursor);
        } else {
            return null;
        }
    }

    public static Friend getFriendFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ID));
        String fullname = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.FULLNAME));
        String email = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.EMAIL));
        String phone = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.PHONE));
        int fileId = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.FILE_ID));
        String avatarUid = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.AVATAR_UID));
        String status = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.STATUS));
        boolean online = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ONLINE)) > 0;
        String type = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.TYPE));

        Friend friend = new Friend(id, fullname, email, phone, fileId, avatarUid, Friend.Type.valueOf(type));
        friend.setStatus(status);
        friend.setOnline(online);

        return friend;
    }

    public static PrivateChat getPrivateChatFromCursor(Cursor cursor) {
        String fullname = cursor.getString(cursor.getColumnIndex(ChatMessagesTable.Cols.OPPONENT_NAME));
        //        int fileId = cursor.getInt(cursor.getColumnIndex(ChatMessagesTable.Cols.FILE_ID));
        String lastMessage = cursor.getString(cursor.getColumnIndex(ChatMessagesTable.Cols.BODY));
        return new PrivateChat(fullname, 0, lastMessage);
    }

    public static boolean searchFriendInBase(Context context, int searchId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + searchId, null, null);
        return cursor.getCount() > Consts.ZERO_VALUE;
    }

    public static Cursor getFriends(Context context, String fullname) {
        Cursor cursor = null;
        String sorting = FriendTable.Cols.ID + " ORDER BY " + FriendTable.Cols.FULLNAME + " COLLATE NOCASE ASC";
        if (TextUtils.isEmpty(fullname)) {
            cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, sorting);
        } else {
            cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                    FriendTable.Cols.FULLNAME + " like '%" + fullname + "%'", null, sorting);
        }
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public static Cursor getAllFriends(Context context) {
        return context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null,
                FriendTable.Cols.ID + " ORDER BY " + FriendTable.Cols.FULLNAME + " COLLATE NOCASE ASC");
    }

    public static void deleteAllFriends(Context context) {
        context.getContentResolver().delete(FriendTable.CONTENT_URI, null, null);
    }

    public static void deleteFriends(Context context, List<Friend> friends) {
        for (Friend friend : friends) {
            deleteFriend(context, friend);
        }
    }

    public static void deleteFriend(Context context, Friend friend) {
        String where = FriendTable.Cols.ID + " = " + friend.getId();
        context.getContentResolver().delete(FriendTable.CONTENT_URI, where, null);
    }

    //--------------------------------------- PrivateChatMessagesTable -----------------------------------------------------

    // TODO SF context, message, senderId, chatId, attachUrl --- to Model
    public static void savePrivateChatMessage(Context context, String message, int senderId, int chatId,
            String attachUrl) {
        ContentValues values = new ContentValues();

        values.put(ChatMessagesTable.Cols.BODY, message);
        values.put(ChatMessagesTable.Cols.SENDER_ID, senderId);
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        // TODO INCOMING
        values.put(ChatMessagesTable.Cols.INCOMING, false);
        values.put(ChatMessagesTable.Cols.CHAT_ID, chatId);
        values.put(ChatMessagesTable.Cols.ATTACH_FILE_URL, attachUrl);

        context.getContentResolver().insert(ChatMessagesTable.CONTENT_URI, values);
    }

    public static void saveGroupChatMessage(Context context, QBChatMessage message, int senderId,
            String groupId) {
        ContentValues values = new ContentValues();
        values.put(ChatMessagesTable.Cols.BODY, message.getBody());
        values.put(ChatMessagesTable.Cols.SENDER_ID, senderId);
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        // TODO INCOMING
        values.put(ChatMessagesTable.Cols.INCOMING, false);
        values.put(ChatMessagesTable.Cols.GROUP_ID, groupId);

        context.getContentResolver().insert(ChatMessagesTable.CONTENT_URI, values);
    }

    public static Cursor getAllGroupChatMessagesByGroupId(Context context, String groupId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null,
                ChatMessagesTable.Cols.GROUP_ID + " = " + "'" + groupId + "'", null, null);
    }

    public static Cursor getAllPrivateChatMessagesByChatId(Context context, int chatId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null,
                ChatMessagesTable.Cols.CHAT_ID + " = " + chatId, null, null);
    }

    public static Cursor getAllPrivateConversations(Context context) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null, null, null, null);
    }

    public static Cursor getAllGroupConversations(Context context) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null,
                "DISTINCT " + ChatMessagesTable.Cols.GROUP_ID, null, ChatMessagesTable.Cols.TIME + " DESC");
    }

    public static void deleteAllChats(Context context) {
        context.getContentResolver().delete(ChatMessagesTable.CONTENT_URI, null, null);
    }
}