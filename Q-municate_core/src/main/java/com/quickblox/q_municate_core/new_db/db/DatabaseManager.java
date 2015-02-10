package com.quickblox.q_municate_core.new_db.db;

import android.content.Context;

import com.quickblox.q_municate_core.new_db.models.Friend;
import com.quickblox.q_municate_core.new_db.models.FriendsRelationStatus;
import com.quickblox.q_municate_core.new_db.models.User;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.sql.SQLException;
import java.util.List;

public class DatabaseManager {

    private static DatabaseManager instance;
    private DatabaseHelper helper;

    private DatabaseManager(Context context) {
        helper = new DatabaseHelper(context);
    }

    public static void init(Context context) {
        if (null == instance) {
            instance = new DatabaseManager(context);
        }
    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    private DatabaseHelper getHelper() {
        return helper;
    }

    // ----------------- USER -----------------
    public User createUser(QBUser qbUser) {
        User user = new User(qbUser);
        try {
            getHelper().getUserDao().create(user);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return user;
    }

    public void updateUser(User user) {
        try {
            getHelper().getUserDao().update(user);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void deleteUser(User user) {
        try {
            getHelper().getUserDao().delete(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User getUserById(int userId) {
        User user = null;
        try {
            user = getHelper().getUserDao().queryForId(userId);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return user;
    }

    public List<User> getAllUsers() {
        List<User> userList = null;
        try {
            userList = getHelper().getUserDao().queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return userList;
    }

    // ----------------- FRIEND -----------------

    public void createFriend(Friend friend) {
        try {
            getHelper().getFriendDao().create(friend);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void refreshFriend(Friend friend) {
        try {
            getHelper().getFriendDao().refresh(friend);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void updateFriend(Friend friend) {
        try {
            getHelper().getFriendDao().update(friend);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void deleteFriend(Friend friend) {
        try {
            getHelper().getFriendDao().delete(friend);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Friend getFriendById(int friendId) {
        Friend friend = null;
        try {
            friend = getHelper().getFriendDao().queryForId(friendId);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friend;
    }

    public List<Friend> getAllFriends() {
        List<Friend> friendList = null;
        try {
            friendList = getHelper().getFriendDao().queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friendList;
    }

    // ----------------- FRIENDS RELATION STATUS -----------------

    public void createFriendsRelationStatus(FriendsRelationStatus friendsRelationStatus) {
        try {
            getHelper().getFriendsRelationStatusDao().create(friendsRelationStatus);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void deleteFriendsRelationStatus(FriendsRelationStatus friendsRelationStatus) {
        try {
            getHelper().getFriendsRelationStatusDao().refresh(friendsRelationStatus);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}