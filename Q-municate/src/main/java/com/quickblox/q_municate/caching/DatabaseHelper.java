package com.quickblox.q_municate.caching;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quickblox.q_municate.caching.tables.MessageTable;
import com.quickblox.q_municate.caching.tables.DialogTable;
import com.quickblox.q_municate.caching.tables.FriendTable;

import java.text.MessageFormat;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String KEY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS {0} ({1})";
    public static final String KEY_DROP_TABLE = "DROP TABLE IF EXISTS {0}";

    private static final int CURRENT_DB_VERSION = 1;
    private static final String DB_NAME = "qmun.db";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, CURRENT_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFriendTable(db);
        createDialogMessageTable(db);
        createDialogTable(db);
        // TODO SF other tables can be created
    }

    private void createFriendTable(SQLiteDatabase db) {
        StringBuilder friendTableFields = new StringBuilder();
        friendTableFields.append(FriendTable.Cols.ID)
                .append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(FriendTable.Cols.FULLNAME).append(" TEXT, ")
                .append(FriendTable.Cols.EMAIL).append(" TEXT, ")
                .append(FriendTable.Cols.PHONE).append(" TEXT, ")
                .append(FriendTable.Cols.FILE_ID).append(" TEXT, ")
                .append(FriendTable.Cols.AVATAR_UID).append(" TEXT, ")
                .append(FriendTable.Cols.STATUS).append(" TEXT, ")
                .append(FriendTable.Cols.ONLINE).append(" TEXT, ")
                .append(FriendTable.Cols.TYPE).append(" TEXT");
        createTable(db, FriendTable.TABLE_NAME, friendTableFields.toString());
    }

    private void createDialogMessageTable(SQLiteDatabase db) {
        StringBuilder dialogMessageTableFields = new StringBuilder();
        dialogMessageTableFields
                .append(MessageTable.Cols.ID).append(" TEXT PRIMARY KEY, ")
                .append(MessageTable.Cols.DIALOG_ID).append(" INTEGER, ")
                .append(MessageTable.Cols.PACKET_ID).append(" TEXT, ")
                .append(MessageTable.Cols.SENDER_ID).append(" INTEGER, ")
                .append(MessageTable.Cols.BODY).append(" TEXT, ")
                .append(MessageTable.Cols.TIME).append(" LONG, ")
                .append(MessageTable.Cols.ATTACH_FILE_ID).append(" TEXT, ")
                .append(MessageTable.Cols.IS_READ).append(" INTEGER, ")
                .append(MessageTable.Cols.IS_DELIVERED).append(" INTEGER");
        createTable(db, MessageTable.TABLE_NAME, dialogMessageTableFields.toString());
    }

    private void createDialogTable(SQLiteDatabase db) {
        StringBuilder dialogTableFields = new StringBuilder();
        dialogTableFields
                .append(DialogTable.Cols.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(DialogTable.Cols.DIALOG_ID).append(" TEXT, ")
                .append(DialogTable.Cols.ROOM_JID_ID).append(" TEXT, ")
                .append(DialogTable.Cols.NAME).append(" TEXT, ")
                .append(DialogTable.Cols.COUNT_UNREAD_MESSAGES).append(" INTEGER, ")
                .append(DialogTable.Cols.LAST_MESSAGE).append(" TEXT, ")
                .append(DialogTable.Cols.LAST_MESSAGE_USER_ID).append(" LONG, ")
                .append(DialogTable.Cols.LAST_DATE_SENT).append(" LONG, ")
                .append(DialogTable.Cols.OCCUPANTS_IDS).append(" TEXT, ")
                .append(DialogTable.Cols.PHOTO_URL).append(" TEXT, ")
                .append(DialogTable.Cols.TYPE).append(" INTEGER");
        createTable(db, DialogTable.TABLE_NAME, dialogTableFields.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db, FriendTable.TABLE_NAME);
        dropTable(db, MessageTable.TABLE_NAME);
        dropTable(db, DialogTable.TABLE_NAME);
        // TODO SF other tables can be dropped
        onCreate(db);
    }

    public void dropTable(SQLiteDatabase db, String name) {
        String query = MessageFormat.format(DatabaseHelper.KEY_DROP_TABLE, name);
        db.execSQL(query);
    }

    public void createTable(SQLiteDatabase db, String name, String fields) {
        String query = MessageFormat.format(DatabaseHelper.KEY_CREATE_TABLE, name, fields);
        db.execSQL(query);
    }
}