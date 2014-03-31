package com.quickblox.qmunicate.caching.orm;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.quickblox.qmunicate.caching.DatabaseWrapper;
import com.quickblox.qmunicate.model.Friend;

public class FriendORM {

    private static final String TAG = "--- sqlite test cache ---";

    private static final String TABLE_NAME = "friends";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static final String COMMA_SEP = ", ";

    private static final String COLUMN_ID_TYPE = "INTEGER PRIMARY KEY";
    private static final String COLUMN_ID = "id";

    private static final String COLUMN_FULLNAME_TYPE = "TEXT";
    private static final String COLUMN_FULLNAME = "fullname";

    private static final String COLUMN_EMAIL_TYPE = "TEXT";
    private static final String COLUMN_EMAIL = "email";

    private static final String COLUMN_PHONE_TYPE = "TEXT";
    private static final String COLUMN_PHONE = "phone";

    private static final String COLUMN_AVATARUID_TYPE = "TEXT";
    private static final String COLUMN_AVATARUID = "avatarUid";

    private static final String COLUMN_STATUS_TYPE = "TEXT";
    private static final String COLUMN_STATUS = "status";

    private static final String COLUMN_LASTREQUEST_TYPE = "TEXT";
    private static final String COLUMN_LASTREQUEST = "lastRequestAt";

    private static final String COLUMN_ISONLINE_TYPE = "TEXT";
    private static final String COLUMN_ISONLINE = "isOnline";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " " + COLUMN_ID_TYPE + COMMA_SEP +
                    COLUMN_FULLNAME + " " + COLUMN_FULLNAME_TYPE + COMMA_SEP +
                    COLUMN_EMAIL + " " + COLUMN_EMAIL_TYPE + COMMA_SEP +
                    COLUMN_PHONE + " " + COLUMN_PHONE_TYPE + COMMA_SEP +
                    COLUMN_AVATARUID + " " + COLUMN_AVATARUID_TYPE + COMMA_SEP +
                    COLUMN_STATUS + " " + COLUMN_STATUS_TYPE + COMMA_SEP +
                    COLUMN_LASTREQUEST + " " + COLUMN_LASTREQUEST_TYPE + COMMA_SEP +
                    COLUMN_ISONLINE + " " + COLUMN_ISONLINE_TYPE +
                    ")";

    public static void insertPost(Context context, Friend post) {
        DatabaseWrapper databaseWrapper = new DatabaseWrapper(context);
        SQLiteDatabase database = databaseWrapper.getWritableDatabase();

        ContentValues values = postToContentValues(post);
        long postId = database.insert(TABLE_NAME, "null", values);
        Log.i(TAG, "Inserted new Post with ID: " + postId);

        database.close();
    }

    /**
     * Packs a Post object into a ContentValues map for use with SQL inserts.
     */
    private static ContentValues postToContentValues(Friend friend) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, friend.getId());
        values.put(COLUMN_FULLNAME, friend.getFullname());
        values.put(COLUMN_EMAIL, friend.getEmail());
        values.put(COLUMN_PHONE, friend.getPhone());
        values.put(COLUMN_AVATARUID, friend.getAvatarUid());
        values.put(COLUMN_STATUS, friend.getStatus());
//        values.put(COLUMN_LASTREQUEST, friend.getLastRequestAt());
//        values.put(COLUMN_AVATARUID, friend.getAvatarUid());
//        values.put(COLUMN_DATE, _dateFormat.format(post.getDate()));

        return values;
    }
}