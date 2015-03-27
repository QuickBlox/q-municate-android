package com.quickblox.q_municate_core.db.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.tables.DialogTable;
import com.quickblox.q_municate_core.db.tables.MessageTable;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DatabaseManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChatDatabaseManager {

    public static QBDialog getDialogByDialogId(Context context, String dialogId) {
        QBDialog dialog = null;
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String resultDialogId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.DIALOG_ID));
            if (!TextUtils.isEmpty(resultDialogId)) {
                dialog = getDialogFromCursor(cursor);
            }
            cursor.close();
        }

        return dialog;
    }

    public static String getPrivateDialogIdByOpponentId(Context context, int opponentId) {
        String dialogId = null;
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.OCCUPANTS_IDS + " like '%" + opponentId + "%'" + " AND " + DialogTable.Cols.TYPE + " = "
                        + QBDialogType.PRIVATE.getCode(), null, null);

        if (cursor != null && cursor.moveToFirst()) {
            dialogId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.DIALOG_ID));
            cursor.close();
        }

        return dialogId;
    }

    public static QBDialog getDialogByRoomJid(Context context, String roomJid) {
        QBDialog dialog = null;
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.ROOM_JID_ID + " = '" + roomJid + "'", null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String resultDialogId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.DIALOG_ID));
            if (!TextUtils.isEmpty(resultDialogId)) {
                dialog = getDialogFromCursor(cursor);
            }
            cursor.close();
        }

        return dialog;
    }

    public static List<QBDialog> getDialogsByOpponent(Context context, int opponent,
                                                      QBDialogType dialogType) {
        List<QBDialog> dialogsList = new LinkedList<QBDialog>();

        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.TYPE + "= ? AND " + DialogTable.Cols.OCCUPANTS_IDS + " like ?",
                new String[]{String.valueOf(dialogType.getCode()), "%" + opponent + "%"}, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                dialogsList.add(getDialogFromCursor(cursor));
            }

            cursor.close();
        }

        return dialogsList;
    }

    public static void saveDialog(Context context, QBDialog dialog) {
        ContentValues values;
        String condition = DialogTable.Cols.DIALOG_ID + "='" + dialog.getDialogId() + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(DialogTable.CONTENT_URI, null, condition, null, null);

        if (cursor != null && cursor.getCount() > ConstsCore.ZERO_INT_VALUE) {
            values = getContentValuesForUpdateDialogTable(context, dialog);
            resolver.update(DialogTable.CONTENT_URI, values, condition, null);
        } else {
            values = getContentValuesForCreateDialogTable(dialog);
            resolver.insert(DialogTable.CONTENT_URI, values);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    public static Cursor getAllDialogs(Context context) {
        return context.getContentResolver().query(DialogTable.CONTENT_URI, null, null, null,
                DialogTable.Cols.ID + " ORDER BY " + DialogTable.Cols.LAST_DATE_SENT + " DESC");
    }

    public static CursorLoader getAllDialogMessagesLoaderByDialogId(Context context, String dialogId) {
        return new CursorLoader(context, MessageTable.CONTENT_URI, null,
                MessageTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null,
                MessageTable.Cols.ID + " ORDER BY " + MessageTable.Cols.TIME + " COLLATE NOCASE ASC");
    }

    public static List<QBDialog> getDialogs(Context context) {
        Cursor allDialogsCursor = getAllDialogs(context);
        List<QBDialog> dialogs = new ArrayList<QBDialog>(allDialogsCursor.getCount());
        if (allDialogsCursor != null) {
            if (allDialogsCursor.getCount() > ConstsCore.ZERO_INT_VALUE) {
                while (allDialogsCursor.moveToNext()) {
                    dialogs.add(getDialogFromCursor(allDialogsCursor));
                }
            }
            allDialogsCursor.close();
        }
        return dialogs;
    }


    public static QBDialog getDialogFromCursor(Cursor cursor) {
        String dialogId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.DIALOG_ID));
        String roomJidId = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.ROOM_JID_ID));
        String name = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.NAME));
        String photoUrl = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.PHOTO_URL));
        String occupantsIdsString = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.OCCUPANTS_IDS));
        ArrayList<Integer> occupantsIdsList = ChatUtils.getOccupantsIdsListFromString(occupantsIdsString);
        int countUnreadMessages = cursor.getInt(cursor.getColumnIndex(
                DialogTable.Cols.COUNT_UNREAD_MESSAGES));
        String lastMessage = cursor.getString(cursor.getColumnIndex(DialogTable.Cols.LAST_MESSAGE));
        Integer lastMessageUserId = cursor.getInt(cursor.getColumnIndex(
                DialogTable.Cols.LAST_MESSAGE_USER_ID));
        long dateSent = cursor.getLong(cursor.getColumnIndex(DialogTable.Cols.LAST_DATE_SENT));
        int type = cursor.getInt(cursor.getColumnIndex(DialogTable.Cols.TYPE));

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJidId);
        dialog.setName(name);
        dialog.setOccupantsIds(occupantsIdsList);
        dialog.setUnreadMessageCount(countUnreadMessages);
        dialog.setLastMessage(lastMessage);
        dialog.setLastMessageUserId(lastMessageUserId);
        dialog.setLastMessageDateSent(dateSent);
        dialog.setPhoto(photoUrl);
        dialog.setType(QBDialogType.parseByCode(type));

        return dialog;
    }

    public static int getCountUnreadDialogs(Context context) {
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.COUNT_UNREAD_MESSAGES + " > 0", null, null);

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public static void deleteAllMessages(Context context) {
        context.getContentResolver().delete(MessageTable.CONTENT_URI, null, null);
    }

    public static void deleteAllDialogs(Context context) {
        context.getContentResolver().delete(DialogTable.CONTENT_URI, null, null);
    }

    public static boolean isExistDialogById(Context context, String dialogId) {
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null, null);

        if (cursor != null && cursor.getCount() > ConstsCore.ZERO_INT_VALUE) {
            cursor.close();
            return true;
        }

        return false;
    }

    public static void deleteMessagesByDialogId(Context context, String dialogId) {
        context.getContentResolver().delete(MessageTable.CONTENT_URI,
                MessageTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null);
    }

    private static ContentValues getContentValuesForUpdateDialogTable(Context context, QBDialog dialog) {
        ContentValues values = new ContentValues();

        if (!TextUtils.isEmpty(dialog.getDialogId())) {
            values.put(DialogTable.Cols.DIALOG_ID, dialog.getDialogId());
        }

        values.put(DialogTable.Cols.NAME, dialog.getName());
        values.put(DialogTable.Cols.PHOTO_URL, dialog.getPhoto());
        values.put(DialogTable.Cols.OCCUPANTS_IDS, ChatUtils.getOccupantsIdsStringFromList(
                dialog.getOccupants()));

        String bodyLastMessage = getLastMessage(context, dialog.getLastMessage(),
                dialog.getLastMessageDateSent());

        if (dialog.getUnreadMessageCount() != ChatUtils.NOT_RESET_COUNTER) {
            values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, dialog.getUnreadMessageCount());
            if (TextUtils.isEmpty(bodyLastMessage)) {
                values.put(DialogTable.Cols.LAST_MESSAGE, bodyLastMessage);
            } else {
                values.put(DialogTable.Cols.LAST_MESSAGE, Html.fromHtml(bodyLastMessage).toString());
            }
        }

        values.put(DialogTable.Cols.LAST_DATE_SENT, dialog.getLastMessageDateSent());

        return values;
    }

    private static String getLastMessage(Context context, String lastMessage, long lastDateSent) {
        return (TextUtils.isEmpty(lastMessage) && lastDateSent != ConstsCore.ZERO_INT_VALUE) ? context.getString(
                R.string.dlg_attached_last_message) : lastMessage;
    }

    private static ContentValues getContentValuesForCreateDialogTable(QBDialog dialog) {
        ContentValues values = new ContentValues();
        values.put(DialogTable.Cols.DIALOG_ID, dialog.getDialogId());
        values.put(DialogTable.Cols.ROOM_JID_ID, dialog.getRoomJid());
        values.put(DialogTable.Cols.NAME, dialog.getName());
        values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, dialog.getUnreadMessageCount());

        String bodyLastMessage = dialog.getLastMessage();

        if (TextUtils.isEmpty(bodyLastMessage)) {
            values.put(DialogTable.Cols.LAST_MESSAGE, bodyLastMessage);
        } else {
            values.put(DialogTable.Cols.LAST_MESSAGE, Html.fromHtml(bodyLastMessage).toString());
        }

        values.put(DialogTable.Cols.LAST_MESSAGE_USER_ID, dialog.getLastMessageUserId());
        values.put(DialogTable.Cols.LAST_DATE_SENT, dialog.getLastMessageDateSent());
        String occupantsIdsString = ChatUtils.getOccupantsIdsStringFromList(dialog.getOccupants());
        values.put(DialogTable.Cols.PHOTO_URL, dialog.getPhoto());
        values.put(DialogTable.Cols.OCCUPANTS_IDS, occupantsIdsString);
        values.put(DialogTable.Cols.TYPE, dialog.getType().getCode());
        return values;
    }

    public static void updateDialog(Context context, String dialogId, int countUnreadMessages) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, countUnreadMessages);
        String condition = DialogTable.Cols.DIALOG_ID + "='" + dialogId + "'";
        resolver.update(DialogTable.CONTENT_URI, values, condition, null);
    }

    public static void updateDialog(Context context, String dialogId, long dateSent,
                                    long lastSenderId, int countUnreadMessages) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = getContentValuesForUpdateDialog(dateSent, lastSenderId,
                countUnreadMessages);
        String condition = DialogTable.Cols.DIALOG_ID + "='" + dialogId + "'";
        resolver.update(DialogTable.CONTENT_URI, values, condition, null);
    }

    public static void updateDialog(Context context, String dialogId, String lastMessage, long dateSent,
                                    long lastSenderId, int countUnreadMessages) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = getContentValuesForUpdateDialog(dateSent, lastSenderId,
                countUnreadMessages);

        if (TextUtils.isEmpty(lastMessage)) {
            values.put(DialogTable.Cols.LAST_MESSAGE, lastMessage);
        } else {
            values.put(DialogTable.Cols.LAST_MESSAGE, Html.fromHtml(lastMessage).toString());
        }

        String condition = DialogTable.Cols.DIALOG_ID + "='" + dialogId + "'";
        resolver.update(DialogTable.CONTENT_URI, values, condition, null);
    }

    private static ContentValues getContentValuesForUpdateDialog(long dateSent,
                                                                 long lastSenderId, int countUnreadMessages) {
        ContentValues values = new ContentValues();

        if (countUnreadMessages >= ConstsCore.ZERO_INT_VALUE) {
            values.put(DialogTable.Cols.COUNT_UNREAD_MESSAGES, countUnreadMessages);
        }

        values.put(DialogTable.Cols.LAST_MESSAGE_USER_ID, lastSenderId);
        values.put(DialogTable.Cols.LAST_DATE_SENT, dateSent);

        return values;
    }

    public static int getCountUnreadMessagesByDialogIdLocal(Context context, String dialogId) {
        Cursor cursor = context.getContentResolver().query(DialogTable.CONTENT_URI, null,
                DialogTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null, null);
        int countMessages = ConstsCore.ZERO_INT_VALUE;

        if (cursor != null && cursor.moveToFirst()) {
            countMessages = cursor.getInt(cursor.getColumnIndex(DialogTable.Cols.COUNT_UNREAD_MESSAGES));
            cursor.close();
        }

        return countMessages;
    }

    public static void clearAllCache(Context context) {
        DatabaseManager.getInstance().clearAllTables();

        deleteAllMessages(context);
        deleteAllDialogs(context);
        // TODO clear something else
    }

    public static void updateMessageStatusDelivered(Context context, String messageId, boolean isDelivered) {
        ContentValues values = new ContentValues();
        String condition = MessageTable.Cols.MESSAGE_ID + "='" + messageId + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(MessageTable.CONTENT_URI, null, condition, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            values.put(MessageTable.Cols.IS_DELIVERED, isDelivered);
            resolver.update(MessageTable.CONTENT_URI, values, condition, null);
            cursor.close();
        }
    }

    public static void deleteDialogByDialogId(Context context, String dialogId) {
        context.getContentResolver().delete(DialogTable.CONTENT_URI,
                DialogTable.Cols.DIALOG_ID + " = '" + dialogId + "'", null);
    }

    public static void deleteDialogByRoomJid(Context context, String roomJid) {
        context.getContentResolver().delete(DialogTable.CONTENT_URI,
                DialogTable.Cols.ROOM_JID_ID + " = '" + roomJid + "'", null);
    }
}