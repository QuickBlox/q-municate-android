package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.AttachmentType;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class AttachmentTypeManager implements CommonDao<AttachmentType> {

    private static final String TAG = AttachmentTypeManager.class.getSimpleName();

    private Dao<AttachmentType, Integer> attachmentTypeDao;

    public AttachmentTypeManager(Dao<AttachmentType, Integer> attachmentTypeDao) {
        this.attachmentTypeDao = attachmentTypeDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(AttachmentType item) {
        try {
            return attachmentTypeDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<AttachmentType> getAll() {
        List<AttachmentType> attachmentTypeList = null;
        try {
            attachmentTypeList = attachmentTypeDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return attachmentTypeList;
    }

    @Override
    public AttachmentType get(int id) {
        AttachmentType attachmentType = null;
        try {
            attachmentType = attachmentTypeDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return attachmentType;
    }

    @Override
    public void update(AttachmentType item) {
        try {
            attachmentTypeDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(AttachmentType item) {
        try {
            attachmentTypeDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public AttachmentType getByAttachmentType(AttachmentType.Type type) {
        AttachmentType attachmentType = null;
        try {
            QueryBuilder<AttachmentType, Integer> queryBuilder = attachmentTypeDao.queryBuilder();
            queryBuilder.where().eq(AttachmentType.COLUMN_TYPE, type);
            PreparedQuery<AttachmentType> preparedQuery = queryBuilder.prepare();
            attachmentType = attachmentTypeDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return attachmentType;
    }
}