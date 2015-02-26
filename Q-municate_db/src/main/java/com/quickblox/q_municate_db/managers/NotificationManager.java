package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Notification;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class NotificationManager implements CommonDao<Notification> {

    private Dao<Notification, Integer> notificationDao;

    public NotificationManager(Dao<Notification, Integer> notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Override
    public Notification createIfNotExists(Notification item) {
        try {
            return notificationDao.createIfNotExists(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    @Override
    public List<Notification> getAll() {
        List<Notification> notificationList = null;
        try {
            notificationList = notificationDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return notificationList;
    }

    @Override
    public Notification get(int id) {
        Notification notification = null;
        try {
            notification = notificationDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return notification;
    }

    @Override
    public void update(Notification item) {
        try {
            notificationDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Notification item) {
        try {
            notificationDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Notification getByNotificationType(Notification.Type type) {
        Notification messageNotificationType = null;
        try {
            QueryBuilder<Notification, Integer> queryBuilder = notificationDao.queryBuilder();
            queryBuilder.where().eq(Notification.COLUMN_NAME_NOTIFICATION, type);
            PreparedQuery<Notification> preparedQuery = queryBuilder.prepare();
            messageNotificationType = notificationDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return messageNotificationType;
    }
}