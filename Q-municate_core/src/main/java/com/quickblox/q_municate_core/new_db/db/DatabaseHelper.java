package com.quickblox.q_municate_core.new_db.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.quickblox.q_municate_core.new_db.models.Friend;
import com.quickblox.q_municate_core.new_db.models.FriendsRelationStatus;
import com.quickblox.q_municate_core.new_db.models.User;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "qmun.sqlite";

    private static final int DATABASE_VERSION = 1;

    private ConcurrentHashMap<Class, Dao> concurrentDaoHashMap = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        concurrentDaoHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Friend.class);
            TableUtils.createTable(connectionSource, FriendsRelationStatus.class);
        } catch (SQLException e) {
            ErrorUtils.logError("Can't create database", e);
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
            int newVersion) {
        try {
            List<String> allSql = new ArrayList<String>();
            switch (oldVersion) {
                case 1:
                    //allSql.add("alter table AdData add column `new_col` VARCHAR");
            }
            for (String sql : allSql) {
                db.execSQL(sql);
            }
        } catch (SQLException e) {
            ErrorUtils.logError("exception during onUpgrade", e);
            throw new RuntimeException(e);
        }
    }

    public Dao getDaoByClass(Class cls) {
        try {
            if (concurrentDaoHashMap.contains(cls)) {
                return concurrentDaoHashMap.get(cls);
            } else {
                concurrentDaoHashMap.put(cls, getDao(cls));
            }
        } catch (java.sql.SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }
}