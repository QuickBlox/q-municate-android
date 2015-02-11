package com.quickblox.q_municate_core.new_db.managers;

import com.j256.ormlite.dao.Dao;
import com.quickblox.q_municate_core.new_db.dao.CommonDao;
import com.quickblox.q_municate_core.new_db.models.Friend;
import com.quickblox.q_municate_core.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class FriendManager implements CommonDao<Friend> {

    private Dao<Friend, Integer> friendDao;

    public FriendManager(Dao<Friend, Integer> friendDao) {
        this.friendDao = friendDao;
    }

    @Override
    public int create(Friend item) {
        try {
            return friendDao.create(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return 0;
    }

    @Override
    public List<Friend> getAll() {
        List<Friend> friendListList = null;
        try {
            friendListList = friendDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friendListList;
    }

    @Override
    public Friend get(int id) {
        Friend friend = null;
        try {
            friend = friendDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return friend;
    }

    @Override
    public void update(Friend item) {
        try {
            friendDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Friend item) {
        try {
            friendDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}