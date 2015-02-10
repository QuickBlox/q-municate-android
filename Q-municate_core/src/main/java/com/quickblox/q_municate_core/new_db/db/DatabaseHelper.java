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

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "qmun.sqlite";

    private static final int DATABASE_VERSION = 1;

    private Dao<User, Integer> userDao = null;
    private Dao<Friend, Integer> friendDao = null;
    private Dao<FriendsRelationStatus, Integer> friendsRelationStatusDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Friend.class);
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

    public Dao<User, Integer> getUserDao() {
        if (null == userDao) {
            try {
                userDao = getDao(User.class);
            } catch (java.sql.SQLException e) {
                ErrorUtils.logError(e);
            }
        }
        return userDao;
    }

    public Dao<Friend, Integer> getFriendDao() {
        if (null == friendDao) {
            try {
                friendDao = getDao(Friend.class);
            } catch (java.sql.SQLException e) {
                ErrorUtils.logError(e);
            }
        }
        return friendDao;
    }

    public Dao<FriendsRelationStatus, Integer> getFriendsRelationStatusDao() {
        if (null == friendsRelationStatusDao) {
            try {
                friendsRelationStatusDao = getDao(FriendsRelationStatus.class);
            } catch (java.sql.SQLException e) {
                ErrorUtils.logError(e);
            }
        }
        return friendsRelationStatusDao;
    }
}