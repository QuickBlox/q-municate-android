package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class DialogNotificationManager implements CommonDao<DialogNotification> {

    private Dao<DialogNotification, Integer> dialogNotificationDao;

    public DialogNotificationManager(Dao<DialogNotification, Integer> dialogNotificationDao) {
        this.dialogNotificationDao = dialogNotificationDao;
    }

    @Override
    public DialogNotification createIfNotExists(DialogNotification item) {
        try {
            return dialogNotificationDao.createIfNotExists(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    @Override
    public List<DialogNotification> getAll() {
        List<DialogNotification> dialogNotificationList = null;
        try {
            dialogNotificationList = dialogNotificationDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogNotificationList;
    }

    @Override
    public DialogNotification get(int id) {
        DialogNotification dialogNotification = null;
        try {
            dialogNotification = dialogNotificationDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogNotification;
    }

    @Override
    public void update(DialogNotification item) {
        try {
            dialogNotificationDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(DialogNotification item) {
        try {
            dialogNotificationDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}