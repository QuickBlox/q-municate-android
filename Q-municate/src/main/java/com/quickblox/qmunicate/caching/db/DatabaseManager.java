package com.quickblox.qmunicate.caching.db;

import android.content.Context;

import com.quickblox.qmunicate.caching.Models.CacheList;
import com.quickblox.qmunicate.model.Friend;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {

	static private DatabaseManager instance;

	static public void init(Context ctx) {
		if (null==instance) {
			instance = new DatabaseManager(ctx);
		}
	}

	static public DatabaseManager getInstance() {
		return instance;
	}

	private DatabaseHelper helper;
	private DatabaseManager(Context ctx) {
		helper = new DatabaseHelper(ctx);
	}

	private DatabaseHelper getHelper() {
		return helper;
	}

	public List<CacheList> getAllCacheLists() {
		List<CacheList> cacheLists = null;
		try {
			cacheLists = getHelper().getCacheListDao().queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cacheLists;
	}

	public void addCacheList(CacheList cacheList) {
		try {
			getHelper().getCacheListDao().create(cacheList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public CacheList getCacheListWithId(int cacheListId) {
		CacheList cacheList = null;
		try {
            cacheList = getHelper().getCacheListDao().queryForId(cacheListId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cacheList;
	}
	

	public Friend getCacheFriendItemWithId(int cacheItemId) {
        Friend cacheList = null;
		try {
			cacheList = getHelper().getCacheFriendItemDao().queryForId(cacheItemId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cacheList;
	}

	public Friend newCacheFriendItem() {
        Friend cacheItem = new Friend();
		try {
			getHelper().getCacheFriendItemDao().create(cacheItem);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cacheItem;
	}

	public void deleteCacheList(CacheList wishList) {
		try {
			getHelper().getCacheListDao().delete(wishList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void refreshCacheList(CacheList cacheList) {
		try {
			getHelper().getCacheListDao().refresh(cacheList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateCacheList(CacheList cacheList) {
		try {
			getHelper().getCacheListDao().update(cacheList);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}