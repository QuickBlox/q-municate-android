package com.quickblox.q_municate_db.managers;

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

public class FriendManager implements CommonDao<Friend> {

    private static final String TAG = FriendManager.class.getSimpleName();

    private Dao<Friend, Integer> friendDao;

    public FriendManager(Dao<Friend, Integer> friendDao) {
        this.friendDao = friendDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Friend item) {
        try {
            return friendDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
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
    }

    @Override
    public void delete(Friend item) {
        try {
            friendDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void delete(int userId) {
        try {
            DeleteBuilder<Friend, Integer> deleteBuilder = friendDao.deleteBuilder();
            deleteBuilder.where().eq(User.Column.ID, userId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
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