package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Observable;

public class FriendManager extends Observable implements CommonDao<Friend> {

    public static final String OBSERVE_FRIEND = "observe_friend";
    private static final String TAG = FriendManager.class.getSimpleName();
    private Handler handler;
    private Dao<Friend, Integer> friendDao;

    public FriendManager(Dao<Friend, Integer> friendDao) {
        handler = new Handler(Looper.getMainLooper());
        this.friendDao = friendDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                FriendManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Friend item) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = null;

        try {
            createOrUpdateStatus = friendDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }

        notifyObservers(OBSERVE_FRIEND);

        return createOrUpdateStatus;
    }

    @Override
    public List<Friend> getAll() {
        List<Friend> friendList = null;
        try {
            friendList = friendDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friendList;
    }

    @Override
    public Friend get(int id) {
        Friend friend = null;
        try {
            friend = friendDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friend;
    }

    @Override
    public void update(Friend item) {
        try {
            friendDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_FRIEND);
    }

    @Override
    public void delete(Friend item) {
        try {
            friendDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_FRIEND);
    }

    public void delete(int userId) {
        try {
            DeleteBuilder<Friend, Integer> deleteBuilder = friendDao.deleteBuilder();
            deleteBuilder.where().eq(User.Column.ID, userId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_FRIEND);
    }

    public Friend getByUserId(int userId) {
        Friend friend = null;
        try {
            QueryBuilder<Friend, Integer> queryBuilder = friendDao.queryBuilder();
            queryBuilder.where().eq(User.Column.ID, userId);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friend = friendDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friend;
    }

    public List<Friend> getFriendsByIds(List<Integer> idsList) {
        List<Friend> friendsList = null;
        try {
            QueryBuilder<Friend, Integer> queryBuilder = friendDao.queryBuilder();
            queryBuilder.where().in(User.Column.ID, idsList);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friendsList = friendDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friendsList;
    }
}