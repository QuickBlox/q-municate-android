package com.quickblox.q_municate_core.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.quickblox.q_municate_core.db.tables.DialogTable;
import com.quickblox.q_municate_core.db.tables.FriendTable;
import com.quickblox.q_municate_core.db.tables.FriendsRelationTable;
import com.quickblox.q_municate_core.db.tables.MessageTable;
import com.quickblox.q_municate_core.db.tables.UserTable;
import com.quickblox.q_municate_core.utils.ConstsCore;

public class DatabaseProvider extends ContentProvider {

    private static final String UNKNOWN_URI = "Unknown URI ";

    private DatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        dbHelper.getWritableDatabase();
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int token = ContentDescriptor.URI_MATCHER.match(uri);

        Cursor result = null;

        switch (token) {
            case UserTable.PATH_TOKEN: {
                result = doQuery(db, uri, UserTable.TABLE_NAME, projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
            case FriendTable.PATH_TOKEN: {
                result = doQuery(db, uri, FriendTable.TABLE_NAME, projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
            case UserTable.USER_FRIEND_PATH_TOKEN: {
                result = doQuery(db, uri, UserTable.TABLE_NAME + ", " + FriendTable.TABLE_NAME, projection,
                        selection, selectionArgs, sortOrder);
                break;
            }
            case FriendsRelationTable.PATH_TOKEN: {
                result = doQuery(db, uri, FriendsRelationTable.TABLE_NAME, projection, selection,
                        selectionArgs, sortOrder);
                break;
            }
            case DialogTable.PATH_TOKEN: {
                result = doQuery(db, uri, DialogTable.TABLE_NAME, projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
            case MessageTable.PATH_TOKEN: {
                result = doQuery(db, uri, MessageTable.TABLE_NAME, projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
        }

        return result;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);

        Uri result = null;

        switch (token) {
            case UserTable.PATH_TOKEN: {
                result = doInsert(db, UserTable.TABLE_NAME, UserTable.CONTENT_URI, uri, values);
                break;
            }
            case FriendTable.PATH_TOKEN: {
                result = doInsert(db, FriendTable.TABLE_NAME, FriendTable.CONTENT_URI, uri, values);
                break;
            }
            case FriendsRelationTable.PATH_TOKEN: {
                result = doInsert(db, FriendsRelationTable.TABLE_NAME, FriendsRelationTable.CONTENT_URI, uri,
                        values);
                break;
            }
            case DialogTable.PATH_TOKEN: {
                result = doInsert(db, DialogTable.TABLE_NAME, DialogTable.CONTENT_URI, uri, values);
                break;
            }
            case MessageTable.PATH_TOKEN: {
                result = doInsert(db, MessageTable.TABLE_NAME, MessageTable.CONTENT_URI, uri, values);
                break;
            }
        }

        if (result == null) {
            throw new IllegalArgumentException(UNKNOWN_URI + uri);
        }

        return result;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        String table = null;
        int token = ContentDescriptor.URI_MATCHER.match(uri);

        switch (token) {
            case UserTable.PATH_TOKEN: {
                table = UserTable.TABLE_NAME;
                break;
            }
            case FriendTable.PATH_TOKEN: {
                table = FriendTable.TABLE_NAME;
                break;
            }
            case FriendsRelationTable.PATH_TOKEN: {
                table = FriendsRelationTable.TABLE_NAME;
                break;
            }
            case DialogTable.PATH_TOKEN: {
                table = DialogTable.TABLE_NAME;
                break;
            }
            case MessageTable.PATH_TOKEN: {
                table = MessageTable.TABLE_NAME;
                break;
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.beginTransaction();

        for (ContentValues cv : values) {
            db.insert(table, null, cv);
        }

        db.setTransactionSuccessful();
        db.endTransaction();

        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);

        int result = ConstsCore.ZERO_INT_VALUE;

        switch (token) {
            case UserTable.PATH_TOKEN: {
                result = doDelete(db, uri, UserTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case FriendTable.PATH_TOKEN: {
                result = doDelete(db, uri, FriendTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case FriendsRelationTable.PATH_TOKEN: {
                result = doDelete(db, uri, FriendsRelationTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case DialogTable.PATH_TOKEN: {
                result = doDelete(db, uri, DialogTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case MessageTable.PATH_TOKEN: {
                result = doDelete(db, uri, MessageTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
        }

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);

        int result = ConstsCore.ZERO_INT_VALUE;

        switch (token) {
            case UserTable.PATH_TOKEN: {
                result = doUpdate(db, uri, UserTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
            case FriendTable.PATH_TOKEN: {
                result = doUpdate(db, uri, FriendTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
            case FriendsRelationTable.PATH_TOKEN: {
                result = doUpdate(db, uri, FriendsRelationTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
            case DialogTable.PATH_TOKEN: {
                result = doUpdate(db, uri, DialogTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
            case MessageTable.PATH_TOKEN: {
                result = doUpdate(db, uri, MessageTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
        }

        return result;
    }

    private Cursor doQuery(SQLiteDatabase db, Uri uri, String tableName, String[] projection,
            String selection, String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(tableName);
        Cursor result = builder.query(db, projection, selection, selectionArgs, sortOrder, null, null);

        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    private int doUpdate(SQLiteDatabase db, Uri uri, String tableName, String selection,
            String[] selectionArgs, ContentValues values) {
        int result = db.update(tableName, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    private int doDelete(SQLiteDatabase db, Uri uri, String tableName, String selection,
            String[] selectionArgs) {
        int result = db.delete(tableName, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    private Uri doInsert(SQLiteDatabase db, String tableName, Uri contentUri, Uri uri, ContentValues values) {
        long id = db.insert(tableName, null, values);
        Uri result = contentUri.buildUpon().appendPath(String.valueOf(id)).build();
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }
}