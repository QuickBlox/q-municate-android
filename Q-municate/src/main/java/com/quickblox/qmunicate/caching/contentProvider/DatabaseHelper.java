package com.quickblox.qmunicate.caching.contentProvider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quickblox.qmunicate.caching.contentProvider.tables.FriendTable;

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

        StringBuilder accountFields = new StringBuilder();

        accountFields.append(FriendTable.Cols.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(FriendTable.Cols.FULLNAME).append(" TEXT, ")
                .append(FriendTable.Cols.EMAIL).append(" TEXT, ")
                .append(FriendTable.Cols.PHONE).append(" TEXT, ")
                .append(FriendTable.Cols.FILE_ID).append(" TEXT, ")
                .append(FriendTable.Cols.AVATAR_UID).append(" TEXT, ")
                .append(FriendTable.Cols.STATUS).append(" TEXT, ")
                .append(FriendTable.Cols.LAST_REQUEST_AT).append(" TEXT, ")
                .append(FriendTable.Cols.ONLINE).append(" TEXT");
        createTable(db, FriendTable.TABLE_NAME, accountFields.toString());

        // TODO SF other tables can be created
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTable(db, FriendTable.TABLE_NAME);
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