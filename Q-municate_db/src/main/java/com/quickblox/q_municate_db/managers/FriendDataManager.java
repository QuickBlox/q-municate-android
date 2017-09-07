package com.quickblox.q_municate_db.managers;

import android.os.Bundle;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class FriendDataManager extends BaseManager<Friend> {

    private static final String TAG = FriendDataManager.class.getSimpleName();

    private Dao<QMUser, Long> userDao;

    public FriendDataManager(Dao<Friend, Long> friendDao, Dao<QMUser, Long> userDao) {
        super(friendDao, FriendDataManager.class.getSimpleName());
        this.userDao = userDao;
    }


    @Override
    public void createOrUpdate(Object object, boolean notify) {
        Friend friend = (Friend) object;
        try {
            int action;

            if(existsByUserId(friend.getUser().getId())){
                dao.update(friend);
                action = UPDATE_ACTION;
            } else{
                dao.create(friend);
                action = CREATE_ACTION;
            }

            if (notify) {
                notifyObservers(friend, action);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate(Friend) - " + e.getMessage());
        }
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putInt(EXTRA_OBJECT_ID, (Integer) id);
    }

    public Friend getByUserId(int userId) {
        Friend friend = null;

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(QMUserColumns.ID, userId);
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
            queryBuilder.where().in(QMUserColumns.ID, idsList);
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
            deleteBuilder.where().eq(QMUserColumns.ID, userId);

            if (deleteBuilder.delete() > 0) {
                //TODO VT need to think how to send ID to observers
                notifyObserversDeletedById(userId);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public boolean existsByUserId(int userId) {
        return getByUserId(userId) != null;
    }

    public List<Friend> getAllForGroupDetails(List<Integer> idsList) {
        List<Friend> friendsList = Collections.emptyList();

        try {
            QueryBuilder<Friend, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().notIn(QMUserColumns.ID, idsList);
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

            QueryBuilder<QMUser, Long> userQueryBuilder = userDao.queryBuilder();
            userQueryBuilder.orderByRaw(QMUserColumns.FULL_NAME + " COLLATE NOCASE");

            friendQueryBuilder.join(userQueryBuilder);

            PreparedQuery<Friend> preparedQuery = friendQueryBuilder.prepare();

            friendsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return friendsList;
    }
}