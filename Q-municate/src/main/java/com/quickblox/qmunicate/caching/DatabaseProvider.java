package com.quickblox.qmunicate.caching;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.quickblox.qmunicate.caching.tables.FriendTable;
import com.quickblox.qmunicate.caching.tables.PrivateChatMessagesTable;
import com.quickblox.qmunicate.model.PrivateChat;

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
            case FriendTable.PATH_TOKEN: {
                result = doQuery(db, uri, FriendTable.TABLE_NAME, projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
            case PrivateChatMessagesTable.PATH_TOKEN: {
                result = doQuery(db, uri, PrivateChatMessagesTable.TABLE_NAME, projection, selection, selectionArgs,
                        sortOrder);
                break;
            }
            // TODO SF other tables can be added
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
            case FriendTable.PATH_TOKEN: {
                result = doInsert(db, FriendTable.TABLE_NAME, FriendTable.CONTENT_URI, uri, values);
                break;
            }
            case PrivateChatMessagesTable.PATH_TOKEN: {
                result = doInsert(db, PrivateChatMessagesTable.TABLE_NAME, PrivateChatMessagesTable.CONTENT_URI, uri, values);
                break;
            }
            // TODO SF other tables can be added
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
            case FriendTable.PATH_TOKEN: {
                table = FriendTable.TABLE_NAME;
                break;
            }
            case PrivateChatMessagesTable.PATH_TOKEN: {
                table = PrivateChatMessagesTable.TABLE_NAME;
                break;
            }
            // TODO SF other tables can be added
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

        int result = 0;

        switch (token) {
            case FriendTable.PATH_TOKEN: {
                result = doDelete(db, uri, FriendTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case PrivateChatMessagesTable.PATH_TOKEN: {
                result = doDelete(db, uri, PrivateChatMessagesTable.TABLE_NAME, selection, selectionArgs);
                break;
            }
            // TODO SF other tables can be added
        }

        return result;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int token = ContentDescriptor.URI_MATCHER.match(uri);

        int result = 0;

        switch (token) {
            case FriendTable.PATH_TOKEN: {
                result = doUpdate(db, uri, FriendTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
            case PrivateChatMessagesTable.PATH_TOKEN: {
                result = doUpdate(db, uri, PrivateChatMessagesTable.TABLE_NAME, selection, selectionArgs, values);
                break;
            }
            // TODO SF other tables can be added
        }

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