package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Status;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class StatusManager implements CommonDao<Status> {

    private static final String TAG = StatusManager.class.getSimpleName();

    private Dao<Status, Integer> statusDao;

    public StatusManager(Dao<Status, Integer> statusDao) {
        this.statusDao = statusDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Status item) {
        try {
            return statusDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Status> getAll() {
        List<Status> statusList = null;
        try {
            statusList = statusDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return statusList;
    }

    @Override
    public Status get(int id) {
        Status status = null;
        try {
            status = statusDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return status;
    }

    @Override
    public void update(Status item) {
        try {
            statusDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Status item) {
        try {
            statusDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Status getByStatusType(Status.Type type) {
        Status status = null;
        try {
            QueryBuilder<Status, Integer> queryBuilder = statusDao.queryBuilder();
            queryBuilder.where().eq(Status.COLUMN_STATUS, type);
            PreparedQuery<Status> preparedQuery = queryBuilder.prepare();
            status = statusDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return status;
    }
}