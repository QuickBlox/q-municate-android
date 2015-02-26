package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.SocialType;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class SocialTypeManager implements CommonDao<SocialType> {

    private Dao<SocialType, Integer> socialTypeDao;

    public SocialTypeManager(Dao<SocialType, Integer> socialTypeDao) {
        this.socialTypeDao = socialTypeDao;
    }

    @Override
    public SocialType createIfNotExists(SocialType item) {
        try {
            return socialTypeDao.createIfNotExists(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    @Override
    public List<SocialType> getAll() {
        List<SocialType> socialTypeList = null;
        try {
            socialTypeList = socialTypeDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return socialTypeList;
    }

    @Override
    public SocialType get(int id) {
        SocialType socialType = null;
        try {
            socialType = socialTypeDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return socialType;
    }

    @Override
    public void update(SocialType item) {
        try {
            socialTypeDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(SocialType item) {
        try {
            socialTypeDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public SocialType getBySocialType(SocialType.Type type) {
        SocialType socialType = null;
        try {
            QueryBuilder<SocialType, Integer> queryBuilder = socialTypeDao.queryBuilder();
            queryBuilder.where().eq(SocialType.COLUMN_TYPE, type);
            PreparedQuery<SocialType> preparedQuery = queryBuilder.prepare();
            socialType = socialTypeDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return socialType;
    }
}