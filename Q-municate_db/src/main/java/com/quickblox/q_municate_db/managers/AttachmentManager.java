package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class AttachmentManager implements CommonDao<Attachment> {

    private static final String TAG = AttachmentManager.class.getSimpleName();

    private Dao<Attachment, Integer> attachmentDao;

    public AttachmentManager(Dao<Attachment, Integer> attachmentDao) {
        this.attachmentDao = attachmentDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Attachment item) {
        try {
            return attachmentDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Attachment> getAll() {
        List<Attachment> attachmentList = null;
        try {
            attachmentList = attachmentDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return attachmentList;
    }

    @Override
    public Attachment get(int id) {
        Attachment attachment = null;
        try {
            attachment = attachmentDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return attachment;
    }

    @Override
    public void update(Attachment item) {
        try {
            attachmentDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Attachment item) {
        try {
            attachmentDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Attachment getByAttachmentType(AttachmentT.Type type) {
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