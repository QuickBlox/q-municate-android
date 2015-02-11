package com.quickblox.q_municate_core.new_db.managers;

import android.content.Context;

import com.quickblox.q_municate_core.new_db.db.DatabaseHelper;
import com.quickblox.q_municate_core.new_db.models.Friend;
import com.quickblox.q_municate_core.new_db.models.User;

public class DatabaseManager {

    private static DatabaseManager instance;
    private DatabaseHelper databaseHelper;

    private DatabaseManager(Context context) {
        databaseHelper = new DatabaseHelper(context);
    }

    public static void init(Context context) {
        if (null == instance) {
            instance = new DatabaseManager(context);
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public FriendManager getFriendManager() {
        return new FriendManager(getDatabaseHelper().getDaoByClass(Friend.class));
    }

    public UserManager getUserManager() {
        return new UserManager(getDatabaseHelper().getDaoByClass(User.class));
    }
}