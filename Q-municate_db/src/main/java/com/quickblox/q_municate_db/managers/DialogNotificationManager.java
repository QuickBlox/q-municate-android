package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Observable;

public class DialogNotificationManager extends Observable implements CommonDao<DialogNotification> {

    private static final String TAG = DialogNotificationManager.class.getSimpleName();
    public static final String OBSERVE_DIALOG_NOTIFICATION = "observe_dialog_notification";

    private Handler handler;
    private Dao<DialogNotification, Integer> dialogNotificationDao;

    public DialogNotificationManager(Dao<DialogNotification, Integer> dialogNotificationDao) {
        handler = new Handler(Looper.getMainLooper());
        this.dialogNotificationDao = dialogNotificationDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                DialogNotificationManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(DialogNotification item) {
        try {
            return dialogNotificationDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
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