package com.quickblox.qmunicate.caching.db;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.quickblox.qmunicate.caching.Models.CacheList;
import com.quickblox.qmunicate.model.Friend;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	private static final String DATABASE_NAME = "CacheListDB.sqlite";
	private static final int DATABASE_VERSION = 1;

	private Dao<CacheList, Integer> cacheListDao = null;
	private Dao<Friend, Integer> cacheFriendsItemDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database,ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, CacheList.class);
			TableUtils.createTable(connectionSource, CacheList.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		} catch (java.sql.SQLException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db,ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			List<String> allSql = new ArrayList<String>(); 
			switch(oldVersion) 
			{
			  case 1: 
				  //allSql.add("alter table AdData add column `new_col` VARCHAR");
				  //allSql.add("alter table AdData add column `new_col2` VARCHAR");
			}
			for (String sql : allSql) {
				db.execSQL(sql);
			}
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "exception during onUpgrade", e);
			throw new RuntimeException(e);
		}
		
	}

    public Dao<CacheList, Integer> getCacheListDao() {
        if (null == cacheListDao) {
            try {
                cacheListDao = getDao(CacheList.class);
            }catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return cacheListDao;
    }

    public Dao<Friend, Integer> getCacheFriendItemDao() {
        if (null == cacheFriendsItemDao) {
            try {
                cacheFriendsItemDao = getDao(Friend.class);
            }catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return cacheFriendsItemDao;
    }
}