package com.quickblox.q_municate_user_cache;


import android.content.Context;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_base_cache.QMAbstractBaseCache;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUserColumns;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.users.model.QBUser;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class QMUserCacheImpl extends QMAbstractBaseCache<QBUser> implements QMUserCache, QMUserColumns {

    private static final String TAG = QMUserCacheImpl.class.getSimpleName();

    private QMUserDataHelper dataHelper;

    public QMUserCacheImpl(Context context) {
        super();
        dataHelper = new QMUserDataHelper(context);
        dao = dataHelper.getDaoByClass(QBUser.class);
    }

    private QMUserDataHelper getDataHelper() {
        return dataHelper;
    }

    @Override
    public void deleteUserByExternalId(String externalId) {
        try {
            DeleteBuilder<QBUser, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(EXTERNAL_ID,externalId);
            PreparedDelete<QBUser> preparedQuery = deleteBuilder.prepare();
            dao.delete(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }



    public List<QBUser> getUsersByIDs(Collection<Integer> idsList) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(ID, idsList);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }



    @Override
    public List<QBUser> getUsersByFilter(Collection<?> filterValue, String filter) {
        return null;
    }

    @Override
    public  QBUser getUserByColumn(String column, String value) {
        QBUser user = null;

        try {
            QueryBuilder<QBUser, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(column, value);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            user = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user;
    }

    @Override
    public List<QBUser> getUsersByColumn(String column, String value) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(column, value);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    @Override
    public List<QBUser> getUsersByColumn(String column, Collection<String> values) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(column, values);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

}
