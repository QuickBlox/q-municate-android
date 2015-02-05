package com.quickblox.q_municate_core.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.db.tables.FriendsRelationTable;
import com.quickblox.q_municate_core.db.tables.MessageTable;
import com.quickblox.q_municate_core.db.tables.DialogTable;
import com.quickblox.q_municate_core.db.tables.UserTable;

import java.text.MessageFormat;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String KEY_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS {0} ({1})";
    public static final String KEY_DROP_TABLE = "DROP TABLE IF EXISTS {0}";

    private Context context;
    private static final int CURRENT_DB_VERSION = 1;
    private static final String DB_NAME = "qmun.db";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, CURRENT_DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUserTable(db);
        createFriendTable(db);
        createFriendsRelationTable(db);
        createMessageTable(db);
        createDialogTable(db);
    }

    private void createUserTable(SQLiteDatabase db) {
        StringBuilder userTableFields = new StringBuilder();
        userTableFields
                .append(UserTable.Cols.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(UserTable.Cols.USER_ID).append(" INTEGER, ")
                .append(UserTable.Cols.FULL_NAME).append(" TEXT, ")
                .append(UserTable.Cols.EMAIL).append(" TEXT, ")
                .append(UserTable.Cols.LOGIN).append(" TEXT, ")
                .append(UserTable.Cols.PHONE).append(" TEXT, ")
                .append(UserTable.Cols.WEB_SITE).append(" TEXT, ")
                .append(UserTable.Cols.CUSTOM_DATA).append(" TEXT, ")
                .append(UserTable.Cols.LAST_REQUEST_AT).append(" TEXT, ")
                .append(UserTable.Cols.EXTERNAL_ID).append(" TEXT, ")
                .append(UserTable.Cols.FACEBOOK_ID).append(" INTEGER, ")
                .append(UserTable.Cols.TWITTER_ID).append(" INTEGER, ")
                .append(UserTable.Cols.BLOB_ID).append(" INTEGER, ")
                .append(UserTable.Cols.AVATAR_URL).append(" TEXT, ")
                .append(UserTable.Cols.STATUS).append(" TEXT, ")
                .append(UserTable.Cols.IS_ONLINE).append(" INTEGER");
        createTable(db, UserTable.TABLE_NAME, userTableFields.toString());
    }

    private void createFriendTable(SQLiteDatabase db) {
        StringBuilder friendTableFields = new StringBuilder();
        friendTableFields
                .append(FriendTable.Cols.ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(FriendTable.Cols.USER_ID).append(" INTEGER, ")
                .append(FriendTable.Cols.IS_PENDING_STATUS).append(" INTEGER, ")
                .append(FriendTable.Cols.IS_NEW_FRIEND_STATUS).append(" INTEGER, ")
                .append(FriendTable.Cols.RELATION_STATUS_ID).append(" INTEGER, FOREIGN KEY (")
                .append(FriendTable.Cols.USER_ID).append(") REFERENCES ")
                .append(UserTable.TABLE_NAME).append(" (")
                .append(UserTable.Cols.USER_ID).append("), FOREIGN KEY (")
                .append(FriendTable.Cols.RELATION_STATUS_ID).append(") REFERENCES ")
                .append(FriendsRelationTable.TABLE_NAME).append(" (")
                .append(FriendsRelationTable.Cols.RELATION_STATUS_ID).append(")");
        createTable(db, FriendTable.TABLE_NAME, friendTableFields.toString());
    }

    private void createFriendsRelationTable(SQLiteDatabase db) {
        StringBuilder friendsRelationTableFields = new StringBuilder();
        friendsRelationTableFields
                .append(FriendsRelationTable.Cols.RELATION_STATUS_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")
                .append(FriendsRelationTable.Cols.RELATION_STATUS).append(" TEXT");
        createTable(db, FriendsRelationTable.TABLE_NAME, friendsRelationTableFields.toString());
        initFriendsRelationTable(db);
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
        dropTable(db, UserTable.TABLE_NAME);
        dropTable(db, FriendTable.TABLE_NAME);
        dropTable(db, FriendsRelationTable.TABLE_NAME);
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

    private void initFriendsRelationTable(SQLiteDatabase db) {
        ContentValues values = new ContentValues();

        String[] relationStatusesArray = context.getResources().getStringArray(R.array.friends_relation_statuses_array);

        for (int i = 0; i < relationStatusesArray.length; i++) {
            values.put(FriendsRelationTable.Cols.RELATION_STATUS, relationStatusesArray[i]);
            db.insert(FriendsRelationTable.TABLE_NAME, null, values);
        }
    }
}