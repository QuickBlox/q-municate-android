package com.quickblox.qmunicate.caching;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.caching.tables.DialogTable;
import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.model.DialogMessage;
import com.quickblox.qmunicate.model.DialogMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

//    private static int unreadMessagesCount;

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
        if (cursor != null && cursor.getCount() > Consts.ZERO_INT_VALUE) {
            resolver.update(FriendTable.CONTENT_URI, values, condition, null);
        } else {
            resolver.insert(FriendTable.CONTENT_URI, values);
        }
        cursor.close();
    }

    public static boolean isFriendInBase(Context context, int searchId) {
        Cursor cursor = context.getContentResolver().query(FriendTable.CONTENT_URI, null,
                FriendTable.Cols.ID + " = " + searchId, null, null);
        return cursor.getCount() > Consts.ZERO_INT_VALUE;
    }

    public static Cursor getFriendsByFullname(Context context, String fullname) {
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

    public static Cursor getAllFriends(Context context) {
        return context.getContentResolver().query(FriendTable.CONTENT_URI, null, null, null,
                FriendTable.Cols.ID + " ORDER BY " + FriendTable.Cols.FULLNAME + " COLLATE NOCASE ASC");
    }

    public static void deleteAllFriends(Context context) {
        context.getContentResolver().delete(FriendTable.CONTENT_URI, null, null);
    }

    public static void saveDialogs(Context context, List<QBDialog> dialogsList) {
        for (QBDialog dialog : dialogsList) {
            if (dialog.getType().equals(QBDialogType.PRIVATE)) {
                String roomJidId = ChatUtils.getOccupantIdFromList(
                        dialog.getOccupants()) + Consts.EMPTY_STRING;
                saveDialog(context, dialog, roomJidId);
            } else {
                saveDialog(context, dialog, dialog.getRoomJid());
            }
        }
    }

    public static QBDialog getDialogByRoomJidId(Context context, String roomJidId) {
        QBDialog dialog = null;
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.ROOM_JID_ID + " = '" + roomJidId + "'", null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String dialogId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.DIALOG_ID));
            if (!TextUtils.isEmpty(dialogId)) {
                dialog = getDialogFromCursor(cursor);
            }
        }
        cursor.close();
        return dialog;
    }

    public static void saveDialog(Context context, QBDialog dialog, String roomJidId) {
//        unreadMessagesCount = Consts.ZERO_INT_VALUE;
        ContentValues values;
        String condition = DialogTable.Cols.ROOM_JID_ID + "='" + roomJidId + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(DialogTable.CONTENT_URI, null, condition, null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_INT_VALUE) {
            values = getContentValuesForUpdateDialogTable(dialog);
            resolver.update(DialogTable.CONTENT_URI, values, condition, null);
        } else {
            values = getContentValuesForCreateDialogTable(dialog, roomJidId);
            resolver.insert(DialogTable.CONTENT_URI, values);
        }
        cursor.close();
    }

    public static Cursor getAllDialogs(Context context) {
        return context.getContentResolver().query(DialogTable.CONTENT_URI, null, null, null,
                DialogTable.Cols.ID + " ORDER BY " + DialogTable.Cols.NAME + " COLLATE NOCASE ASC");
    }

    public static QBDialog createTempDialogByRoomJidId(Context context, String roomJidId) {
        QBDialog dialog = new QBDialog();
        dialog.setRoomJid(roomJidId);
        Friend opponentFriend = getFriendById(context, Integer.parseInt(roomJidId));
        dialog.setName(opponentFriend.getFullname());
        ArrayList<Integer> occupantsIdsList = ChatUtils.getOccupantsIdsListForCreatePrivateDialog(opponentFriend.getId());
        dialog.setOccupantsIds(occupantsIdsList);
        dialog.setType(QBDialogType.PRIVATE);
        dialog.setLastMessageDateSent(DateUtils.getCurrentTime());
        saveDialog(context, dialog, roomJidId);
        return dialog;
    }

    public static QBDialog getDialogFromCursor(Cursor cursor) {
        String dialogId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.DIALOG_ID));
        String roomJidId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.ROOM_JID_ID));
        String name = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.NAME));
        String occupantsIdsString = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.OCCUPANTS_IDS));
        ArrayList<Integer> occupantsIdsList = ChatUtils.getOccupantsIdsListFromString(occupantsIdsString);
        int countUnreadMessages = cursor.getInt(cursor.getColumnIndex(
                DialogTable.Cols.COUNT_UNREAD_MESSAGES));
        String lastMessage = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.LAST_MESSAGE));
        long dateSent = cursor.getLong(cursor.getColumnIndex(
                DialogTable.Cols.LAST_DATE_SENT));
        String type = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.TYPE));

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJidId);
        dialog.setName(name);
        dialog.setOccupantsIds(occupantsIdsList);
        dialog.setUnreadMessageCount(countUnreadMessages);
        dialog.setLastMessage(lastMessage);
        dialog.setLastMessageDateSent(dateSent);
        dialog.setType(QBDialogType.valueOf(type));

        return dialog;
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

    public static int getCountUnreadDialogs(Context context) {
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.COUNT_UNREAD_MESSAGES + " > 0", null, null);
        return cursor.getCount();
    }

    public static Cursor getAllDialogMessagesByRoomJidId(Context context, String roomJidId) {
        return context.getContentResolver().query(DialogMessageTable.CONTENT_URI, null,
                DialogMessageTable.Cols.ROOM_JID_ID + " = '" + roomJidId + "'", null, null);
    }

    public static void deleteAllMessages(Context context) {
        context.getContentResolver().delete(DialogMessageTable.CONTENT_URI, null, null);
    }

    public static void deleteAllDialogs(Context context) {
        context.getContentResolver().delete(DialogTable.CONTENT_URI, null, null);
    }

    public static void saveChatMessages(Context context, List<QBHistoryMessage> messagesList,
                                        String roomJidId, boolean isGroupMessage) {
        for (QBHistoryMessage historyMessage : messagesList) {
            String message = historyMessage.getBody();
            int senderId = historyMessage.getSenderId();
            String attachURL;

            if (TextUtils.isEmpty(message)) {
                attachURL = ChatUtils.getAttachUrlFromMessage(historyMessage.getAttachments());
            } else {
                attachURL = Consts.EMPTY_STRING;
            }

            if (TextUtils.isEmpty(message) && TextUtils.isEmpty(attachURL)) {
                if(isGroupMessage) {
                    message = getMessageForNotification(context, senderId);
                } else {
                    return;
                }
            }

            DialogMessageCache dialogMessageCache = new DialogMessageCache(roomJidId, senderId, message,
                    attachURL, historyMessage.getDateSent());

            saveChatMessage(context, dialogMessageCache);
        }
    }

    private static String getMessageForNotification(Context context, int senderId) {
        String message;
        Friend friend = DatabaseManager.getFriendById(context, senderId);
        if(friend == null) {
            message = context.getResources().getString(R.string.user_created_room, senderId);
        } else {
            message = context.getResources().getString(R.string.user_created_room, friend.getFullname());
        }
        return message;
    }

    public static void saveChatMessage(Context context, DialogMessageCache dialogMessageCache) {
//        unreadMessagesCount++;
        ContentValues values = new ContentValues();
        values.put(DialogMessageTable.Cols.ROOM_JID_ID, dialogMessageCache.getRoomJidId());
        values.put(DialogMessageTable.Cols.SENDER_ID, dialogMessageCache.getSenderId());
        values.put(DialogMessageTable.Cols.BODY, dialogMessageCache.getMessage());
        values.put(DialogMessageTable.Cols.TIME, dialogMessageCache.getTime());
        values.put(DialogMessageTable.Cols.ATTACH_FILE_ID, dialogMessageCache.getAttachUrl());
        values.put(DialogMessageTable.Cols.IS_READ, false);
        context.getContentResolver().insert(DialogMessageTable.CONTENT_URI, values);
//        updateDialog(context, dialogMessageCache.getRoomJidId(), dialogMessageCache.getMessage());
    }

    public static void deleteMessagesByRoomJidId(Context context, String roomJidId) {
        context.getContentResolver().delete(DialogMessageTable.CONTENT_URI,
                DialogMessageTable.Cols.ROOM_JID_ID + " = '" + roomJidId + "'", null);
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

    private static ContentValues getContentValuesForUpdateDialogTable(QBDialog dialog) {
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            values.put(DialogTable.Cols.DIALOG_ID, dialog.getDialogId());
        }
        if (!TextUtils.isEmpty(dialog.getLastMessage())) {
            values.put(DialogTable.Cols.LAST_MESSAGE, dialog.getLastMessage());
        }
        values.put(DialogTable.Cols.LAST_DATE_SENT, dialog.getLastMessageDateSent());
        values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, dialog.getUnreadMessageCount());
        return values;
    }

    private static ContentValues getContentValuesForCreateDialogTable(QBDialog dialog, String roomJidId) {
        ContentValues values = new ContentValues();
        values.put(DialogTable.Cols.DIALOG_ID, dialog.getDialogId());
        values.put(DialogTable.Cols.ROOM_JID_ID, roomJidId);
        values.put(DialogTable.Cols.NAME, dialog.getName());
        values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, dialog.getUnreadMessageCount());
        values.put(DialogTable.Cols.LAST_MESSAGE, dialog.getLastMessage());
        values.put(DialogTable.Cols.LAST_DATE_SENT, dialog.getLastMessageDateSent());
        String[] occupantsIdsArray = ChatUtils.getOccupantsIdsArrayFromList(dialog.getOccupants());
        String occupantsIdsString = ChatUtils.getOccupantsIdsStringFromArray(occupantsIdsArray);
        values.put(DialogTable.Cols.OCCUPANTS_IDS, occupantsIdsString);
        values.put(DialogTable.Cols.TYPE, dialog.getType().name());
        return values;
    }

    public static void updateDialog(Context context, QBDialog dialog, String roomJidId) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, getCountUnreadMessagesByDialog(context, dialog));
        values.put(DialogTable.Cols.LAST_MESSAGE, dialog.getLastMessage());
        values.put(DialogTable.Cols.LAST_DATE_SENT, dialog.getLastMessageDateSent());
        String condition = DialogTable.Cols.ROOM_JID_ID + "='" + roomJidId + "'";
        resolver.update(DialogTable.CONTENT_URI, values, condition, null);
    }

    public static int getCountUnreadMessagesByDialog(Context context, QBDialog dialog) {
        Cursor cursor = context.getContentResolver().query(DialogMessageTable.CONTENT_URI, null,
                DialogMessageTable.Cols.IS_READ + " = 0", null, null);
        return cursor.getCount();
    }

    private static List<Friend> getFriendListFromCursor(Cursor cursor) {
        if (cursor.getCount() > Consts.ZERO_INT_VALUE) {
            List<Friend> friendList = new ArrayList<Friend>(cursor.getCount());
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                friendList.add(getFriendFromCursor(cursor));
            }
            cursor.close();
            return friendList;
        }
        return null;
    }

    public static void clearAllCache(Context context) {
        DatabaseManager.deleteAllFriends(context);
        DatabaseManager.deleteAllMessages(context);
        DatabaseManager.deleteAllDialogs(context);
        // TODO SF clear something else
    }

    public static void updateStatusMessage(Context context, String messageId, boolean isRead) {
        ContentValues values = new ContentValues();
        String condition = DialogMessageTable.Cols.ID + "='" + messageId + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(DialogMessageTable.CONTENT_URI, null, condition, null, null);
        if (cursor != null && cursor.getCount() > Consts.ZERO_INT_VALUE) {
            values.put(DialogMessageTable.Cols.IS_READ, isRead);
            resolver.update(DialogMessageTable.CONTENT_URI, values, condition, null);
            cursor.close();
        }
    }
}