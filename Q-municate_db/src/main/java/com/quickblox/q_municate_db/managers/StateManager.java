package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class StateManager implements CommonDao<State> {

    private static final String TAG = StateManager.class.getSimpleName();

    private Dao<State, Integer> stateDao;

    public StateManager(Dao<State, Integer> stateDao) {
        this.stateDao = stateDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(State item) {
        try {
            return stateDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<State> getAll() {
        List<State> stateList = null;
        try {
            stateList = stateDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return stateList;
    }

    @Override
    public State get(int id) {
        State state = null;
        try {
            state = stateDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return state;
    }

    @Override
    public void update(State item) {
        try {
            stateDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(State item) {
        try {
            stateDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public State getByStateType(State.Type type) {
        State state = null;
        try {
            QueryBuilder<State, Integer> queryBuilder = stateDao.queryBuilder();
            queryBuilder.where().eq(State.COLUMN_STATE, type);
            PreparedQuery<State> preparedQuery = queryBuilder.prepare();
            state = stateDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return state;
    }
}