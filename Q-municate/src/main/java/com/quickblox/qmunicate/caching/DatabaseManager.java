package com.quickblox.qmunicate.caching;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.qmunicate.caching.tables.ChatMessageTable;
import com.quickblox.qmunicate.caching.tables.ChatTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.ChatCache;
import com.quickblox.qmunicate.model.ChatMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    public static void saveFriends(Context context, List<Friend> friendsList) {
        for (Friend friend : friendsList) {
            saveFriend(context, friend);
        }
    }

    public static void saveFriend(Context context, Friend friend) {
        ContentValues values = getContentValuesFriendTable(friend);

        String condition = FriendTable.Cols.ID + "='" + friend.getId() + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(FriendTable.CONTENT_URI, null, condition, null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_VALUE) {
            resolver.update(FriendTable.CONTENT_URI, values, condition, null);
        } else {
            resolver.insert(FriendTable.CONTENT_URI, values);
        }
    }

    private static ContentValues getContentValuesFriendTable(Friend friend) {
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

    public static void saveChats(Context context, List<ChatCache> chatCacheList) {
        for (ChatCache chatCache : chatCacheList) {
            saveChat(context, chatCache);
        }
    }

    public static void saveChat(Context context, ChatCache chatCache) {
        ContentValues values = getContentValuesChatTable(chatCache);
        String condition = ChatTable.Cols.DIALOG_ID + "='" + chatCache.getDialogId() + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(ChatTable.CONTENT_URI, null, condition, null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_VALUE) {
            resolver.update(ChatTable.CONTENT_URI, values, condition, null);
        } else {
            resolver.insert(ChatTable.CONTENT_URI, values);
        }
    }

    private static ContentValues getContentValuesChatTable(ChatCache chatCache) {
        ContentValues values = new ContentValues();
        values.put(ChatTable.Cols.DIALOG_ID, chatCache.getDialogId());
        values.put(ChatTable.Cols.JID_ID, chatCache.getRoomJidId());
        values.put(ChatTable.Cols.NAME, chatCache.getName());
        values.put(ChatTable.Cols.COUNT_UNREAD_MESSAGES, chatCache.getCountUnreadMessages());
        values.put(ChatTable.Cols.LAST_MESSAGE, chatCache.getLastMessage());
        String occupantsIds = ChatUtils.occupantIdsToStringFromArray(chatCache.getOccupantsIds());
        values.put(ChatTable.Cols.OCCUPANTS_IDS, occupantsIds);
        values.put(ChatTable.Cols.TYPE, chatCache.getType());
        return values;
    }

    public static ChatCache getChatCacheFromCursor(Cursor cursor) {
        String dialogId = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.DIALOG_ID));
        String jidId = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.JID_ID));
        String name = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.NAME));
        String[] occupantsIdsArray = (cursor.getString(cursor.getColumnIndex(ChatTable.Cols.OCCUPANTS_IDS)))
                .split(ChatUtils.OCCUPANT_IDS_DIVIDER);
        int countUnreadMessages = cursor.getInt(cursor.getColumnIndex(ChatTable.Cols.COUNT_UNREAD_MESSAGES));
        String lastMessage = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.LAST_MESSAGE));
        int type = cursor.getInt(cursor.getColumnIndex(ChatTable.Cols.TYPE));

        ChatCache chatCache = new ChatCache(dialogId, jidId, name, countUnreadMessages, lastMessage,
                occupantsIdsArray, type);

        return chatCache;
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

    public static boolean searchFriendInBase(Context context, int searchId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + searchId, null, null);
        return cursor.getCount() > Consts.ZERO_VALUE;
    }

    public static Cursor getFriends(Context context, String fullname) {
        Cursor cursor;
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

    public static Cursor getAllChats(Context context) {
        return context.getContentResolver().query(ChatTable.CONTENT_URI, null, null, null,
                ChatTable.Cols.ID + " ORDER BY " + ChatTable.Cols.NAME + " COLLATE NOCASE ASC");
    }

    public static int getCountUnreadChatsDialogs(Context context) {
        Cursor cursor = context.getContentResolver().query(ChatTable.CONTENT_URI, null,
                ChatTable.Cols.COUNT_UNREAD_MESSAGES + " > 0", null, null);
        return cursor.getCount();
    }

    public static boolean isChatDialogInBase(Context context, String dialogId) {
        Cursor cursor = context.getContentResolver().query(ChatTable.CONTENT_URI, null,
                ChatTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null, null);
        return cursor.getCount() > Consts.ZERO_VALUE;
    }

    public static Cursor getAllFriends(Context context) {
        return context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null,
                FriendTable.Cols.ID + " ORDER BY " + FriendTable.Cols.FULLNAME + " COLLATE NOCASE ASC");
    }

    public static void deleteAllFriends(Context context) {
        context.getContentResolver().delete(FriendTable.CONTENT_URI, null, null);
    }

    public static Cursor getAllGroupChatMessagesByGroupId(Context context, String groupId) {
        return context.getContentResolver().query(ChatMessageTable.CONTENT_URI, null,
                ChatMessageTable.Cols.GROUP_ID + " = " + "'" + groupId + "'", null, null);
    }

    public static Cursor getAllPrivateChatMessagesByChatId(Context context, int chatId) {
        return context.getContentResolver().query(ChatMessageTable.CONTENT_URI, null,
                ChatMessageTable.Cols.CHAT_ID + " = " + chatId, null, null);
    }

    public static void deleteAllMessages(Context context) {
        context.getContentResolver().delete(ChatMessageTable.CONTENT_URI, null, null);
    }

    public static void deleteAllChats(Context context) {
        context.getContentResolver().delete(ChatTable.CONTENT_URI, null, null);
    }

    public static void saveChatMessages(Context context, List<QBHistoryMessage> messagesList, Object chatId) {
        for (QBHistoryMessage message : messagesList) {
            String body = message.getBody();
            int senderId = message.getSenderId();
            String attachURL;

            if (TextUtils.isEmpty(body)) {
                attachURL = ChatUtils.getAttachUrlFromQBChatMessage(message);
            } else {
                attachURL = Consts.EMPTY_STRING;
            }

            ChatMessageCache chatMessageCache = null;
            if (chatId instanceof String) {
                chatMessageCache = new ChatMessageCache(body, senderId, (String) chatId, attachURL);
            } else if (chatId instanceof Integer) {
                chatMessageCache = new ChatMessageCache(body, senderId, (Integer) chatId, attachURL);
            }

            saveChatMessage(context, chatMessageCache);
        }
    }

    public static void saveChatMessage(Context context, ChatMessageCache chatMessageCache) {
        ContentValues values = new ContentValues();
        values.put(ChatMessageTable.Cols.BODY, chatMessageCache.getMessage());
        values.put(ChatMessageTable.Cols.SENDER_ID, chatMessageCache.getSenderId());
        values.put(ChatMessageTable.Cols.TIME, System.currentTimeMillis());
        values.put(ChatMessageTable.Cols.ATTACH_FILE_ID, chatMessageCache.getAttachUrl());
        if (chatMessageCache.getRoomJid() != null) {
            values.put(ChatMessageTable.Cols.GROUP_ID, chatMessageCache.getRoomJid());
        } else if (chatMessageCache.getChatId() != null) {
            values.put(ChatMessageTable.Cols.CHAT_ID, chatMessageCache.getChatId());
        }
        context.getContentResolver().insert(ChatMessageTable.CONTENT_URI, values);
    }

    public static void deleteMessagesByChatId(Context context, int chatId) {
        context.getContentResolver().delete(ChatMessageTable.CONTENT_URI,
                ChatMessageTable.Cols.CHAT_ID + " = " + chatId, null);
    }

    public static void deleteMessagesByGroupId(Context context, String groupId) {
        context.getContentResolver().delete(ChatMessageTable.CONTENT_URI,
                ChatMessageTable.Cols.GROUP_ID + " = '" + groupId + "'", null);
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
}