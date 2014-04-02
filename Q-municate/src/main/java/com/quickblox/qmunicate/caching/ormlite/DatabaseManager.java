package com.quickblox.qmunicate.caching.ormlite;

import android.content.Context;

import com.quickblox.qmunicate.model.Friend;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {

    private static DatabaseManager instance;
    private DatabaseHelper helper;

    private DatabaseManager(Context ctx) {
        helper = new DatabaseHelper(ctx);
    }

    public static void init(Context ctx) {
        if (null == instance) {
            instance = new DatabaseManager(ctx);
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    public void addCacheFriend(Friend friend) {
        try {
            getHelper().getCacheFriendsDao().create(friend);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DatabaseHelper getHelper() {
        return helper;
    }

    public void deleteCachedFriendsList(List<Friend> cacheList) {
        try {
            getHelper().getCacheFriendsDao().delete(cacheList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Friend> getCachedFriendsList() {
        List<Friend> cacheLists = null;
        try {
            cacheLists = getHelper().getCacheFriendsDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cacheLists;
    }
}