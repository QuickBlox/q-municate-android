package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FriendDataManager extends BaseManager<Friend> {

    private static final String TAG = FriendDataManager.class.getSimpleName();

    private Dao<User, Long> userDao;

    public FriendDataManager(Dao<Friend, Long> friendDao, Dao<User, Long> userDao) {
        super(friendDao, FriendDataManager.class.getSimpleName());
        this.userDao = userDao;
    }

    public Friend getByUserId(int userId) {
        Friend friend = null;

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.ID, userId);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friend = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friend;
    }

    public List<Friend> getAllByIds(List<Integer> idsList) {
        List<Friend> friendsList = Collections.emptyList();

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(User.Column.ID, idsList);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friendsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friendsList;
    }

    public void deleteByUserId(int userId) {
        try {
            DeleteBuilder<Friend, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(User.Column.ID, userId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_KEY);
    }

    public boolean existsByUserId(int userId) {
        return getByUserId(userId) != null;
    }

    public List<Friend> getAllForGroupDetails(List<Integer> idsList) {
        List<Friend> friendsList = Collections.emptyList();

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().notIn(User.Column.ID, idsList);
            PreparedQuery<Friend> preparedQuery = queryBuilder.prepare();
            friendsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friendsList;
    }

    public List<Friend> getAllSorted() {
        List<Friend> friendsList = Collections.emptyList();

        try {
            QueryBuilder<Friend, Long> friendQueryBuilder = dao.queryBuilder();

            QueryBuilder<User, Long> userQueryBuilder = userDao.queryBuilder();
            userQueryBuilder.orderByRaw(User.Column.FULL_NAME + " COLLATE NOCASE");

            friendQueryBuilder.join(userQueryBuilder);

            PreparedQuery<Friend> preparedQuery = friendQueryBuilder.prepare();

            friendsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friendsList;
    }
}