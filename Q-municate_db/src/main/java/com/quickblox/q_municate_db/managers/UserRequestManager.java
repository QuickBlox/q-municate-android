package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import java.util.Observable;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class UserRequestManager extends Observable implements CommonDao<UserRequest> {

    private static final String TAG = UserRequestManager.class.getSimpleName();
    public static final String OBSERVE_USER_REQUEST = "observe_user_request";

    private Handler handler;
    private Dao<UserRequest, Integer> userRequestDao;

    public UserRequestManager(Dao<UserRequest, Integer> userRequestDao) {
        handler = new Handler(Looper.getMainLooper());
        this.userRequestDao = userRequestDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                UserRequestManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(UserRequest item) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = null;

        try {
            createOrUpdateStatus = userRequestDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }

        notifyObservers(OBSERVE_USER_REQUEST);

        return createOrUpdateStatus;
    }

    @Override
    public List<UserRequest> getAll() {
        List<UserRequest> userRequestList = null;
        try {
            userRequestList = userRequestDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return userRequestList;
    }

    @Override
    public UserRequest get(int id) {
        UserRequest userRequest = null;
        try {
            userRequest = userRequestDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return userRequest;
    }

    @Override
    public void update(UserRequest item) {
        try {
            userRequestDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_USER_REQUEST);
    }

    @Override
    public void delete(UserRequest item) {
        try {
            userRequestDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_USER_REQUEST);
    }

    public User getUserById(int userId) {
        UserRequest userRequest = null;

        try {
            QueryBuilder<UserRequest, Integer> queryBuilder = userRequestDao.queryBuilder();
            queryBuilder.where().eq(User.Column.ID, userId);
            PreparedQuery<UserRequest> preparedQuery = queryBuilder.prepare();
            userRequest = userRequestDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return userRequest != null ? userRequest.getUser() : null;
    }

    public void deleteByUserId(int userId) {
        try {
            DeleteBuilder<UserRequest, Integer> deleteBuilder = userRequestDao.deleteBuilder();
            deleteBuilder.where().eq(User.Column.ID, userId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_USER_REQUEST);
    }
}