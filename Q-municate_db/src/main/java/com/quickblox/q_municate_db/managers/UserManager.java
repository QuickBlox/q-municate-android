package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public class UserManager extends Observable implements CommonDao<User> {

    private static final String TAG = UserManager.class.getSimpleName();
    public static final String OBSERVE_USER = "observe_user";

    private Handler handler;
    private Dao<User, Integer> userDao;

    public UserManager(Dao<User, Integer> userDao) {
        handler = new Handler(Looper.getMainLooper());
        this.userDao = userDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                UserManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(User item) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = null;

        try {
            createOrUpdateStatus = userDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }

        notifyObservers(OBSERVE_USER);

        return createOrUpdateStatus;
    }

    public void createOrUpdate(final Collection<User> userList) {
        try {
            userDao.callBatchTasks(new Callable<User>() {
                @Override
                public User call() throws Exception {
                    for (User user : userList) {
                        userDao.createOrUpdate(user);
                    }

                    notifyObservers(OBSERVE_USER);

                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        List<User> userList = null;
        try {
            userList = userDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return userList;
    }

    @Override
    public User get(int id) {
        User user = null;
        try {
            user = userDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return user;
    }

    @Override
    public void update(User item) {
        try {
            userDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_USER);
    }

    @Override
    public void delete(User item) {
        try {
            userDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_USER);
    }

    public boolean isUserOwner(String email) {
        User user = null;

        try {
            QueryBuilder<User, Integer> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().eq(User.Column.EMAIL, email);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            user = userDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user != null && user.getRole() == User.Role.OWNER;
    }
}