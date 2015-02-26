package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class SocialManager implements CommonDao<Social> {

    private Dao<Social, Integer> socialDao;

    public SocialManager(Dao<Social, Integer> socialDao) {
        this.socialDao = socialDao;
    }

    @Override
    public Social createIfNotExists(Social item) {
        try {
            return socialDao.createIfNotExists(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    @Override
    public List<Social> getAll() {
        List<Social> socialList = null;
        try {
            socialList = socialDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return socialList;
    }

    @Override
    public Social get(int id) {
        Social social = null;
        try {
            social = socialDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return social;
    }

    @Override
    public void update(Social item) {
        try {
            socialDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Social item) {
        try {
            socialDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Social getBySocialId(String socialId) {
        Social social = null;
        try {
            QueryBuilder<Social, Integer> queryBuilder = socialDao.queryBuilder();
            queryBuilder.where().eq(Social.COLUMN_SOCIAL_ID, socialId);
            PreparedQuery<Social> preparedQuery = queryBuilder.prepare();
            social = socialDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return social;
    }
}