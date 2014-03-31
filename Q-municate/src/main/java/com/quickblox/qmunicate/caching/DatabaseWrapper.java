package com.quickblox.qmunicate.caching;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.quickblox.qmunicate.caching.orm.FriendORM;

public class DatabaseWrapper extends SQLiteOpenHelper {

    private static final String TAG = "--- sqlite test cache ---";

    private static final String DATABASE_NAME = "qmun_cache.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseWrapper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(TAG, "Creating database [" + DATABASE_NAME + " v." + DATABASE_VERSION + "]...");

        sqLiteDatabase.execSQL(FriendORM.SQL_CREATE_TABLE);
        sqLiteDatabase.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database [" + DATABASE_NAME + " v." + oldVersion + "] to [" + DATABASE_NAME + " v." + newVersion + "]...");

        sqLiteDatabase.execSQL(FriendORM.SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}