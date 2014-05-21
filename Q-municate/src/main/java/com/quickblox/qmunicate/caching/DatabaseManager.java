package com.quickblox.qmunicate.caching;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;



import android.util.Log;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.qmunicate.caching.tables.ChatTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;

import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.*;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.model.PrivateChatMessageCache;
import com.quickblox.qmunicate.utils.Consts;
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

    public static Chat getChatFromCursor(Cursor cursor, Context context){
        Chat chat;
        String chatName = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.CHAT_NAME));
        String membersIds = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.MEMBERS_IDS));
        String lastMessage = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.LAST_MESSAGE));
        String avatarUid = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.AVATAR_ID));
        List<String> friendsIds = new ArrayList<String>();

        int isGroup = cursor.getInt(cursor.getColumnIndex(ChatTable.Cols.IS_GROUP));
        //TODO: Sometimes causes crash, logging will be improved later.
//        Log.i("ChatName", membersIds);
        if(isGroup == 1){
            String[] friendsArray = membersIds.split("_");
            for(String friend : friendsArray){
                Log.i("ChatName", "Adding friend: " + friend + ", List size: " + friendsArray.length);
                friendsIds.add(friend);
            }
            List<Friend> friends = getFriendsById(context, friendsArray);
            chat = new GroupChat(chatName, avatarUid == null ? 0 : Integer.parseInt(avatarUid));
            chat.setLastMessage(lastMessage);
            ((GroupChat)chat).setOpponents(friends);
        } else {
            chat = new PrivateChat(chatName, avatarUid == null ? 0 : Integer.parseInt(avatarUid), lastMessage);
            String friendId = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.CHAT_ID));
            Log.i("ChatName", "Adding friend: " + friendId);
            Friend friend = getFriendById(context, friendId);
            ((PrivateChat)chat).setFriend(friend);
        }
        return chat;
    }

    public static List<Friend> getFriendsById(Context context, String[] ids){
        String selection = "";
        for(String id : ids){
            selection += "?, ";
        }
        selection = selection.substring(0, selection.length() - 2);
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI,
                null,
                FriendTable.Cols.ID + " in " + "(" + selection + ")",
                ids,
                null);
        if(cursor.getCount() > 0){
            List<Friend> friends = new ArrayList<Friend>(cursor.getCount());
            for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
                int id = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ID));
                String fullname = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.FULLNAME));
                String email = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.EMAIL));
                String phone = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.PHONE));
                int fileId = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.FILE_ID));
                String avatarUid = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.AVATAR_UID));
                long lastRequestAt = cursor.getLong(cursor.getColumnIndex(FriendTable.Cols.LAST_REQUEST_AT));
                friends.add(new Friend(id, fullname, email, phone, fileId, avatarUid, DateUtils.longToDate(
                        lastRequestAt)));
            }
            cursor.close();
            Log.i("Members IDs", "Returned friends size: " + friends.size());
            return friends;
        }
        return null;
    }

    private static Friend getFriendById(Context context, String friendId){
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, FriendTable.Cols.ID + " = " + "'" + friendId + "'", null, null);
        Log.i("ChatName", "Cursor length: " + cursor.getCount());
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
        } else{
            return null;
        }
        int id = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.ID));
        String fullname = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.FULLNAME));
        String email = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.EMAIL));
        String phone = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.PHONE));
        int fileId = cursor.getInt(cursor.getColumnIndex(FriendTable.Cols.FILE_ID));
        String avatarUid = cursor.getString(cursor.getColumnIndex(FriendTable.Cols.AVATAR_UID));
        long lastRequestAt = cursor.getLong(cursor.getColumnIndex(FriendTable.Cols.LAST_REQUEST_AT));
        Log.i("ChatName", "getFriendById FullName: " + fullname);
        cursor.close();
        return new Friend(id, fullname, email, phone, fileId, avatarUid, DateUtils.longToDate(
                lastRequestAt));
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

    public static Cursor fetchFriendsByFullname(Context context, String inputText) {
        Cursor cursor = null;
        String sorting = FriendTable.Cols.ID + " ORDER BY " + FriendTable.Cols.FULLNAME + " COLLATE NOCASE ASC";
        if (TextUtils.isEmpty(inputText)) {
            cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null, sorting);
        } else {
            cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                    FriendTable.Cols.FULLNAME + " like '%" + inputText + "%'", null, sorting);
        }
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public static Cursor getCursorFriendById(Context context, int friendId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + friendId, null, null);
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

    public static void savePrivateChatMessage(Context context, PrivateChatMessageCache privateChatMessageCache) {
        ContentValues values = new ContentValues();

        values.put(ChatMessagesTable.Cols.BODY, privateChatMessageCache.getMessage());
        values.put(ChatMessagesTable.Cols.SENDER_ID, privateChatMessageCache.getSenderId());
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        values.put(ChatMessagesTable.Cols.INCOMING, false);
        values.put(ChatMessagesTable.Cols.CHAT_ID, privateChatMessageCache.getChatId());
        values.put(ChatMessagesTable.Cols.ATTACH_FILE_URL, privateChatMessageCache.getAttachUrl());

        context.getContentResolver().insert(ChatMessagesTable.CONTENT_URI, values);
        ContentValues chatValues = new ContentValues();
        chatValues.put(ChatTable.Cols.CHAT_ID, privateChatMessageCache.getChatId());
        chatValues.put(ChatTable.Cols.CHAT_NAME, privateChatMessageCache.getOpponentName());
        chatValues.put(ChatTable.Cols.LAST_MESSAGE, privateChatMessageCache.getMessage());
        chatValues.put(ChatTable.Cols.IS_GROUP, 0);
        Cursor c = context.getContentResolver().query(ChatTable.CONTENT_URI, null, ChatTable.Cols.CHAT_NAME + "='" + privateChatMessageCache.getOpponentName() + "'", null, null);
        if (c.getCount() == 1) {
            context.getContentResolver().update(ChatTable.CONTENT_URI, chatValues, ChatTable.Cols.CHAT_ID + "='" + privateChatMessageCache.getChatId(), null);
        } else {
            context.getContentResolver().insert(ChatTable.CONTENT_URI, chatValues);
        }
    }

    public static void saveGroupChatMessage(Context context, QBChatMessage message, int senderId, String groupId, String membersIds) {
        Log.i("GroupMessage: ", " saveGroupChatMessage");
        ContentValues values = new ContentValues();
        values.put(ChatMessagesTable.Cols.BODY, message.getBody());
        values.put(ChatMessagesTable.Cols.SENDER_ID, senderId);
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        values.put(ChatMessagesTable.Cols.INCOMING, false);
        values.put(ChatMessagesTable.Cols.GROUP_ID, groupId);

        context.getContentResolver().insert(ChatMessagesTable.CONTENT_URI, values);
        ContentValues chatValues = new ContentValues();
        chatValues.put(ChatTable.Cols.CHAT_ID, groupId);
        chatValues.put(ChatTable.Cols.CHAT_NAME, groupId);
        chatValues.put(ChatTable.Cols.MEMBERS_IDS, membersIds);
        chatValues.put(ChatTable.Cols.LAST_MESSAGE, message.getBody());
        chatValues.put(ChatTable.Cols.IS_GROUP, 1);
        Cursor c = context.getContentResolver().query(ChatTable.CONTENT_URI, null, ChatTable.Cols.CHAT_ID + "='" + groupId + "'", null, null);
        if (c != null && c.getCount() == 1) {
            Log.i("GroupMessage: ", " There's already");
            context.getContentResolver().update(ChatTable.CONTENT_URI, chatValues, ChatTable.Cols.CHAT_ID + "='" + groupId + "'", null);
        } else {
            Log.i("GroupMessage: ", "There's not yet");
            context.getContentResolver().insert(ChatTable.CONTENT_URI, chatValues);
        }
    }

    public static Cursor getAllGroupChatMessagesByGroupId(Context context, String groupId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null,
                ChatMessagesTable.Cols.GROUP_ID + " = " + "'" + groupId + "'", null, null);
    }

    public static Cursor getAllChatConversations(Context context){
        return context.getContentResolver().query(ChatTable.CONTENT_URI, null, null, null, null);
    }

    public static Cursor getAllPrivateChatMessagesByChatId(Context context, int chatId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null, ChatMessagesTable.Cols.CHAT_ID + " = " + chatId, null, null);
    }

    public static void deleteAllChats(Context context) {
        context.getContentResolver().delete(ChatMessagesTable.CONTENT_URI, null, null);
    }
}