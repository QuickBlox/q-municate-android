package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public class DialogManager extends Observable implements CommonDao<Dialog> {

    private static final String TAG = DialogManager.class.getSimpleName();
    public static final String OBSERVE_DIALOG = "observe_dialog";

    private Handler handler;
    private Dao<Dialog, Integer> dialogDao;

    public DialogManager(Dao<Dialog, Integer> dialogDao) {
        handler = new Handler(Looper.getMainLooper());
        this.dialogDao = dialogDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                DialogManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Dialog item) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = null;

        try {
            createOrUpdateStatus = dialogDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }

        notifyObservers(OBSERVE_DIALOG);

        return createOrUpdateStatus;
    }

    @Override
    public List<Dialog> getAll() {
        List<Dialog> dialogList = null;
        try {
            dialogList = dialogDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogList;
    }

    @Override
    public Dialog get(int id) {
        Dialog dialog = null;
        try {
            dialog = dialogDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialog;
    }

    @Override
    public void update(Dialog item) {
        try {
            dialogDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_DIALOG);
    }

    @Override
    public void delete(Dialog item) {
        try {
            dialogDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_DIALOG);
    }

    public void delete(String dialogId) {
        try {
            DeleteBuilder<Dialog, Integer> deleteBuilder = dialogDao.deleteBuilder();
            deleteBuilder.where().eq(Dialog.Column.ID, dialogId);
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        notifyObservers(OBSERVE_DIALOG);
    }

    public void createOrUpdate(final Collection<Dialog> dialogsList) {
        try {
            dialogDao.callBatchTasks(new Callable<Dialog>() {
                @Override
                public Dialog call() throws Exception {
                    for (Dialog dialog : dialogsList) {
                        dialogDao.createOrUpdate(dialog);
                    }

                    notifyObservers(OBSERVE_DIALOG);

                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
    }

    public Dialog getByDialogId(String dialogId) {
        Dialog dialog = null;
        try {
            QueryBuilder<Dialog, Integer> queryBuilder = dialogDao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId);
            PreparedQuery<Dialog> preparedQuery = queryBuilder.prepare();
            dialog = dialogDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialog;
    }
}