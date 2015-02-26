package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class UserRequestManager implements CommonDao<UserRequest> {

    private Dao<UserRequest, Integer> userRequestDao;

    public UserRequestManager(Dao<UserRequest, Integer> userRequestDao) {
        this.userRequestDao = userRequestDao;
    }

    @Override
    public UserRequest createIfNotExists(UserRequest item) {
        try {
            return userRequestDao.createIfNotExists(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
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
    }

    @Override
    public void delete(UserRequest item) {
        try {
            userRequestDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}