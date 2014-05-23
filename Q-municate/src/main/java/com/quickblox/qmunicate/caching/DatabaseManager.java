package com.quickblox.qmunicate.caching;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.caching.tables.ChatTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.Chat;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.GroupChat;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.model.PrivateChatMessageCache;
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

    public static Chat getChatFromCursor(Cursor cursor, Context context) {
        Chat chat;
        String chatName = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.CHAT_NAME));
        String membersIds = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.MEMBERS_IDS));
        String lastMessage = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.LAST_MESSAGE));
        String avatarUid = cursor.getString(cursor.getColumnIndex(ChatTable.Cols.AVATAR_ID));
        List<String> friendsIdsList = new ArrayList<String>();

        boolean isGroup = cursor.getInt(cursor.getColumnIndex(ChatTable.Cols.IS_GROUP)) > Consts.ZERO_VALUE;
        int avatarId = 0;
        if (isGroup) {
            String[] friendsArray = membersIds.split(",");
            for (String friend : friendsArray) {
                friendsIdsList.add(friend);
            }
            List<Friend> friendList = getFriendListByIds(context, friendsArray);
            if (avatarUid != null) {
                avatarId = Integer.parseInt(avatarUid);
            }
            chat = new GroupChat(chatName, avatarId);
            chat.setLastMessage(lastMessage);
            ((GroupChat) chat).setOpponents(friendList);
        } else {
            if (avatarUid != null) {
                avatarId = Integer.parseInt(avatarUid);
            }
            chat = new PrivateChat(chatName, avatarId, lastMessage);
            int friendId = cursor.getInt(cursor.getColumnIndex(ChatTable.Cols.CHAT_ID));
            Friend friend = getFriendById(context, friendId);
            ((PrivateChat) chat).setFriend(friend);
        }
        return chat;
    }

    public static List<Friend> getFriendListByIds(Context context, String[] ids) {
        String selection = getSelection(ids);
        selection = selection.substring(0, selection.length() - 2);
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

    public static void saveChatMessage(Context context, PrivateChatMessageCache privateChatMessageCache) {
        ContentValues values = new ContentValues();
        values.put(ChatMessagesTable.Cols.BODY, privateChatMessageCache.getMessage());
        values.put(ChatMessagesTable.Cols.SENDER_ID, privateChatMessageCache.getSenderId());
        values.put(ChatMessagesTable.Cols.TIME, System.currentTimeMillis());
        values.put(ChatMessagesTable.Cols.INCOMING, false);
        values.put(ChatMessagesTable.Cols.ATTACH_FILE_URL, privateChatMessageCache.getAttachUrl());
        if(privateChatMessageCache.isGroup()){
            values.put(ChatMessagesTable.Cols.GROUP_ID, privateChatMessageCache.getChatId());
            values.put(ChatMessagesTable.Cols.CHAT_ID, privateChatMessageCache.getChatId());
        } else{
            values.put(ChatMessagesTable.Cols.CHAT_ID, Integer.parseInt(privateChatMessageCache.getChatId()));
        }
        context.getContentResolver().insert(ChatMessagesTable.CONTENT_URI, values);

        ContentValues chatValues = new ContentValues();
        chatValues.put(ChatTable.Cols.CHAT_ID, privateChatMessageCache.getChatId());
        chatValues.put(ChatTable.Cols.CHAT_NAME, privateChatMessageCache.getOpponentName());
        chatValues.put(ChatTable.Cols.LAST_MESSAGE, privateChatMessageCache.getMessage());
        chatValues.put(ChatTable.Cols.IS_GROUP, privateChatMessageCache.isGroup() ? 1 : 0);
        Cursor cursor = context.getContentResolver().query(ChatTable.CONTENT_URI, null, ChatTable.Cols.CHAT_NAME + "='" + privateChatMessageCache.getOpponentName() + "'", null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_VALUE) {
            context.getContentResolver().update(ChatTable.CONTENT_URI, chatValues, ChatTable.Cols.CHAT_ID + "='" + privateChatMessageCache.getChatId() + "'", null);
        } else {
            if(privateChatMessageCache.isGroup()){
                chatValues.put(ChatTable.Cols.MEMBERS_IDS, privateChatMessageCache.getMembersIds());
            }
            context.getContentResolver().insert(ChatTable.CONTENT_URI, chatValues);
        }
    }

    public static void saveChats(Context context, List<QBDialog> dialogsList) {
        for (QBDialog dialog : dialogsList) {
            saveChat(context, dialog);
        }
    }

    public static void saveChat(Context context, QBDialog dialog) {

        ContentValues chatValues = new ContentValues();
        chatValues.put(ChatTable.Cols.CHAT_ID, dialog.getId());
        chatValues.put(ChatTable.Cols.CHAT_NAME, dialog.getName());
        chatValues.put(ChatTable.Cols.LAST_MESSAGE, dialog.getLastMessage());
        chatValues.put(ChatTable.Cols.IS_GROUP, isChatGroup(dialog.getType()) ? 1 : 0);
        Cursor cursor = context.getContentResolver().query(ChatTable.CONTENT_URI, null, ChatTable.Cols.CHAT_ID + "='" + dialog.getId() + "'", null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_VALUE) {
            context.getContentResolver().update(ChatTable.CONTENT_URI, chatValues, ChatTable.Cols.CHAT_ID + "='" + dialog.getId() + "'", null);
        } else {
            if(isChatGroup(dialog.getType())){
                chatValues.put(ChatTable.Cols.MEMBERS_IDS, dialog.getOccupants().toArray().toString());
            }
            context.getContentResolver().insert(ChatTable.CONTENT_URI, chatValues);
        }

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

    private static boolean isChatGroup(QBDialogType dialogType) {
        boolean isGroup = false;
        switch (dialogType) {
            case GROUP:
                isGroup = true;
                break;
            case PRIVATE:
                isGroup = false;
                break;
            case PUBLIC_GROUP:
                isGroup = true;
                break;
        }
        return isGroup;
    }

    public static Cursor getAllGroupChatMessagesByGroupId(Context context, String groupId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null,
                ChatMessagesTable.Cols.GROUP_ID + " = " + "'" + groupId + "'", null, null);
    }

    public static Cursor getAllChatConversations(Context context) {
        return context.getContentResolver().query(ChatTable.CONTENT_URI, null, null, null, null);
    }

    public static Cursor getAllPrivateChatMessagesByChatId(Context context, int chatId) {
        return context.getContentResolver().query(ChatMessagesTable.CONTENT_URI, null,
                ChatMessagesTable.Cols.CHAT_ID + " = " + chatId, null, null);
    }

    public static void deleteAllChats(Context context) {
        context.getContentResolver().delete(ChatMessagesTable.CONTENT_URI, null, null);
    }

    private static List<Friend> getFriendListFromCursor(Cursor cursor) {
        if (cursor.getCount() > 0) {
            List<Friend> friendList = new ArrayList<Friend>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                friendList.add(getFriendFromCursor(cursor));
            }
            cursor.close();
            return friendList;
        }
        return null;
    }

    private static String getSelection(String[] iDs) {
        String selection = "";
        for (String id : iDs) {
            selection += "?, ";
        }
        return selection;
    }
}