package com.quickblox.q_municate_db.managers;

import android.os.Bundle;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.sql.SQLException;

public class UserRequestDataManager extends BaseManager<UserRequest> {

    private static final String TAG = UserRequestDataManager.class.getSimpleName();

    public UserRequestDataManager(Dao<UserRequest, Long> userRequestDao) {
        super(userRequestDao, UserRequestDataManager.class.getSimpleName());
    }

    @Override
    public void createOrUpdate(Object object, boolean notify) {
        UserRequest userRequest = (UserRequest) object;
        try {
            int action;

            if(existsByUserId(userRequest.getUser().getId())){
                dao.update(userRequest);
                action = UPDATE_ACTION;
            } else{
                dao.create(userRequest);
                action = CREATE_ACTION;
            }

            if (notify) {
                notifyObservers(userRequest, action);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate(UserRequest) - " + e.getMessage());
        }
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putInt(EXTRA_OBJECT_ID, (Integer) id);
    }

    public QMUser getUserRequestById(int userId) {
        UserRequest userRequest = null;

        try {
            QueryBuilder<UserRequest, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(QMUserColumns.ID, userId);
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
        return getUserRequestById(userId) != null;
    }
}