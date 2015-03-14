package com.quickblox.q_municate_core.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quickblox.q_municate_core.db.tables.DialogTable;
import com.quickblox.q_municate_core.db.tables.MessageTable;

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
        createMessageTable(db);
        createDialogTable(db);
    }

    private void createMessageTable(SQLiteDatabase db) {
        StringBuilder messageTableFields = new StringBuilder();
        messageTableFields
                .append(MessageTable.Cols.ID).append(" INTEGER PRIMARY KEY, ")
                .append(MessageTable.Cols.MESSAGE_ID).append(" TEXT, ")
                .append(MessageTable.Cols.DIALOG_ID).append(" TEXT, ")
                .append(MessageTable.Cols.SENDER_ID).append(" INTEGER, ")
                .append(MessageTable.Cols.RECIPIENT_ID).append(" INTEGER, ")
                .append(MessageTable.Cols.BODY).append(" TEXT, ")
                .append(MessageTable.Cols.TIME).append(" LONG, ")
                .append(MessageTable.Cols.ATTACH_FILE_ID).append(" TEXT, ")
                .append(MessageTable.Cols.IS_READ).append(" INTEGER, ")
                .append(MessageTable.Cols.IS_DELIVERED).append(" INTEGER, ")
                .append(MessageTable.Cols.IS_SYNC).append(" INTEGER, ")
                .append(MessageTable.Cols.FRIENDS_NOTIFICATION_TYPE).append(" INTEGER");
        createTable(db, MessageTable.TABLE_NAME, messageTableFields.toString());
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
        dropTable(db, MessageTable.TABLE_NAME);
        dropTable(db, DialogTable.TABLE_NAME);
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