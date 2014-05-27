package com.quickblox.qmunicate.caching;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.ChatMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.model.PrivateChatMessageCache;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final int intTrue = 1;

    public static void saveFriends(Context context, List<Friend> friendsList) {
        for (Friend friend : friendsList) {
            saveFriend(context, friend);
        }
    }

    public static void saveFriend(Context context, Friend friend) {
        ContentValues values = getContentValues(friend);

        String condition = FriendTable.Cols.ID + "='" + friend.getId() + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(FriendTable.CONTENT_URI, null, condition, null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_VALUE) {
            resolver.update(FriendTable.CONTENT_URI, values, condition, null);
        } else {
            resolver.insert(FriendTable.CONTENT_URI, values);
        }
    }

    private static ContentValues getContentValues(Friend friend) {
        ContentValues values = new ContentValues();
        values.put(FriendTable.Cols.ID, friend.getId());
        values.put(FriendTable.Cols.FULLNAME, friend.getFullname());
        values.put(FriendTable.Cols.EMAIL, friend.getEmail());
        values.put(FriendTable.Cols.PHONE, friend.getPhone());
        values.put(FriendTable.Cols.FILE_ID, friend.getFileId());
        values.put(FriendTable.Cols.AVATAR_UID, friend.getAvatarUrl());
        values.put(FriendTable.Cols.STATUS, friend.getOnlineStatus());
        values.put(FriendTable.Cols.ONLINE, friend.isOnline());
        values.put(FriendTable.Cols.TYPE, friend.getType().name());
        return values;
    }

    public static Friend getFriendById(Context context, int friendId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + friendId, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            Friend friend = getFriendFromCursor(cursor);
            cursor.close();
            return friend;
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

        // TODO IS set Cursor as argument to constructor
        Friend friend = new Friend(id, fullname, email, phone, fileId, avatarUid, Friend.Type.valueOf(type));
        friend.setStatus(status);
        friend.setOnline(online);

        return friend;
    }

    public static List<Friend> getFriendListByIds(Context context, String[] ids) {
        String selection = getSelection(ids);
        selection = selection.substring(Consts.ZERO_VALUE, selection.length() - 2);
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " in " + "(" + selection + ")", ids, null);
        return getFriendListFromCursor(cursor);
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

    public static void saveChatMessage(Context context, ChatMessageCache chatMessageCache) {
        ContentValues values = new ContentValues();
        values.put(ChatMessagesTable.Cols.BODY, chatMessageCache.getMessage());
        values.put(ChatMessagesTable.Cols.SENDER_ID, chatMessageCache.getSenderId());
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        values.put(ChatMessagesTable.Cols.ATTACH_FILE_ID, chatMessageCache.getAttachUrl());
        if (chatMessageCache.getRoomJid() != null) {
            values.put(ChatMessagesTable.Cols.GROUP_ID, chatMessageCache.getRoomJid());
        }
        if (chatMessageCache.getChatId() != null) {
            values.put(ChatMessagesTable.Cols.CHAT_ID, chatMessageCache.getChatId());
        }
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

    public static void deleteAllChats(Context context) {
        context.getContentResolver().delete(ChatMessagesTable.CONTENT_URI, null, null);
    }

    private static List<Friend> getFriendListFromCursor(Cursor cursor) {
        if (cursor.getCount() > Consts.ZERO_VALUE) {
            List<Friend> friendList = new ArrayList<Friend>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                friendList.add(getFriendFromCursor(cursor));
            }
            cursor.close();
            return friendList;
        }
        return null;
    }

    private static String getSelection(String[] ids) {
        String selection = "";
        for (String id : ids) {
            selection += "?, ";
        }
        return selection;
    }

    public static void saveChatMessages(Context context, List<QBHistoryMessage> messagesList, int chatId) {
        for (QBHistoryMessage message : messagesList) {
            String body = message.getBody();
            int senderId = message.getSenderId();
            String attachURL;

            if (TextUtils.isEmpty(body)) {
                attachURL = ChatUtils.getAttachUrlFromQBChatMessage(message);
            } else {
                attachURL = Consts.EMPTY_STRING;
            }

            String opponentName = null;
            String membersIds = Consts.EMPTY_STRING;

            PrivateChatMessageCache privateChatMessageCache = new PrivateChatMessageCache(body, senderId, chatId + Consts.EMPTY_STRING, attachURL, opponentName, membersIds);
            saveChatMessage(context, privateChatMessageCache);
        }
    }

    public static void deleteMessagesByDialog(Context context, String chatId) {
        context.getContentResolver().delete(ChatMessagesTable.CONTENT_URI, ChatMessagesTable.Cols.CHAT_ID + " = " + chatId, null);
    }
}