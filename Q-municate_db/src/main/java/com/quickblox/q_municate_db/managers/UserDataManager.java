package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class UserDataManager extends BaseManager<User> {

    private static final String TAG = UserDataManager.class.getSimpleName();

    public UserDataManager(Dao<User, Long> userDao) {
        super(userDao, UserDataManager.class.getSimpleName());
    }

    public boolean isUserOwner(String email) {
        User user = null;

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.EMAIL, email);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            user = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user != null && user.getRole() == User.Role.OWNER;
    }

    public User getOwner() {
        User user = null;

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.ROLE, User.Role.OWNER);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            user = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user;
    }

    public List<User> getAllByIds(List<Integer> idsList) {
        List<User> usersList  = Collections.emptyList();

        try {
            QueryBuilder<User, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(User.Column.ID, idsList);
            PreparedQuery<User> preparedQuery = queryBuilder.prepare();
            usersList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }
}