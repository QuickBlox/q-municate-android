package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;

public class UserRequestDataManager extends BaseManager<UserRequest> {

    private static final String TAG = UserRequestDataManager.class.getSimpleName();

    public UserRequestDataManager(Dao<UserRequest, Long> userRequestDao) {
        super(userRequestDao, UserRequestDataManager.class.getSimpleName());
    }

    public User getUserRequestById(int userId) {
        UserRequest userRequest = null;

        try {
            QueryBuilder<UserRequest, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(User.Column.ID, userId);
            PreparedQuery<UserRequest> preparedQuery = queryBuilder.prepare();
            userRequest = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return userRequest != null ? userRequest.getUser() : null;
    }

    public void deleteByUserId(int userId) {
        try {
            DeleteBuilder<UserRequest, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(User.Column.ID, userId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_KEY);
    }

    public boolean existsByUserId(int userId) {
        return getUserRequestById(userId) != null;
    }
}