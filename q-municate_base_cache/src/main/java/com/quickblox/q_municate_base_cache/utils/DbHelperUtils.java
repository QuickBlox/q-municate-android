package com.quickblox.q_municate_base_cache.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbHelperUtils {

    private static final String TAG = DbHelperUtils.class.getSimpleName();

    public static final String TABLES_QUERY = "SELECT name FROM sqlite_master WHERE type='table'";
    public static final String DROP_QUERY = "DROP TABLE ";

    private static final List<String> SERVICE_TABLES = Arrays.asList("sqlite_sequence", "android_metadata");

    public static List<String> getTables(Cursor cursor) {
        List<String> tables = new ArrayList<>(cursor.getCount());
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            String table = cursor.getString(0);
            if (!SERVICE_TABLES.contains(table)) {
                tables.add(table);
            }
        }
        return tables;
    }

    public static void onCreate(ConnectionSource connectionSource, Class<?>[] tables) {
        try {
            for (Class clazz : tables) {
                TableUtils.createTable(connectionSource, clazz);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public static void onOpen(SQLiteDatabase db) {
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    public static void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, Class<?>[] tablesClasses) {
        try {
            Cursor c = database.rawQuery(DbHelperUtils.TABLES_QUERY, null);
            List<String> tables = DbHelperUtils.getTables(c);
            c.close();

            database.beginTransaction();
            try {
                for (String t : tables) {
                    try {
                        database.execSQL(DbHelperUtils.DROP_QUERY + t);
                        Log.d(TAG, "Drop table " + t);
                    } catch (Exception e) {
                        Log.e(TAG, "Error while dropping table " + t, e);
                    }
                }
                onCreate(connectionSource, tablesClasses);
                database.setTransactionSuccessful();
            } finally {
                database.endTransaction();
            }
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public static void clearTable(ConnectionSource connectionSource, Class clazz) {
        try {
            TableUtils.clearTable(connectionSource, clazz);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public static void clearTables(ConnectionSource connectionSource, Class<?>[] tables) {
        for (Class clazz : tables) {
            clearTable(connectionSource, clazz);
        }
    }
}