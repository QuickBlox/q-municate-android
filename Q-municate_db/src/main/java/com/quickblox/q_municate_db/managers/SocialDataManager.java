package com.quickblox.q_municate_db.managers;

import android.os.Bundle;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;

public class SocialDataManager extends BaseManager<Social> {

    private static final String TAG = SocialDataManager.class.getSimpleName();

    public SocialDataManager(Dao<Social, Long> socialDao) {
        super(socialDao, SocialDataManager.class.getSimpleName());
    }

    public Social getBySocialId(String socialId) {
        Social social = null;

        try {
            QueryBuilder<Social, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(Social.Column.ID, socialId);
            PreparedQuery<Social> preparedQuery = queryBuilder.prepare();
            social = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return social;
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putString(EXTRA_OBJECT_ID, (String) id);
    }
}