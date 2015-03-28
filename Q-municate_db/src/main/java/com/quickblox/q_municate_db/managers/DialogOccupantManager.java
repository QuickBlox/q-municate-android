package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public class DialogOccupantManager extends Observable implements CommonDao<DialogOccupant> {

    private static final String TAG = DialogOccupantManager.class.getSimpleName();
    public static final String OBSERVE_DIALOG_OCCUPANT = "observe_dialog_occupant";

    private Handler handler;
    private Dao<Dialog, Integer> dialogDao;
    private Dao<DialogOccupant, Integer> dialogOccupantDao;

    public DialogOccupantManager(Dao<DialogOccupant, Integer> dialogOccupantDao, Dao<Dialog, Integer> dialogDao) {
        handler = new Handler(Looper.getMainLooper());
        this.dialogOccupantDao = dialogOccupantDao;
        this.dialogDao = dialogDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                DialogOccupantManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(DialogOccupant item) {
        try {
            return dialogOccupantDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<DialogOccupant> getAll() {
        List<DialogOccupant> dialogOccupantList = null;
        try {
            dialogOccupantList = dialogOccupantDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogOccupantList;
    }

    @Override
    public DialogOccupant get(int id) {
        DialogOccupant dialogOccupant = null;
        try {
            dialogOccupant = dialogOccupantDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogOccupant;
    }

    @Override
    public void update(DialogOccupant item) {
        try {
            dialogOccupantDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(DialogOccupant item) {
        try {
            dialogOccupantDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void createOrUpdate(final Collection<DialogOccupant> dialogOccupantsList) {
        try {
            dialogOccupantDao.callBatchTasks(new Callable<DialogOccupant>() {
                @Override
                public DialogOccupant call() throws Exception {
                    for (DialogOccupant dialogOccupant : dialogOccupantsList) {
                        dialogOccupantDao.createOrUpdate(dialogOccupant);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
    }

    public List<DialogOccupant> getDialogOccupantsListByDialogId(String dialogId) {
        List<DialogOccupant> dialogOccupantsList = null;
        try {
            QueryBuilder<DialogOccupant, Integer> queryBuilder = dialogOccupantDao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId);
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupantsList = dialogOccupantDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogOccupantsList;
    }

    public DialogOccupant getDialogOccupantForPrivateChat(int userId) {
        DialogOccupant dialogOccupant = null;
        try {
            QueryBuilder<DialogOccupant, Integer> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();
            dialogOccupantQueryBuilder.where().eq(User.Column.ID, userId);

            QueryBuilder<Dialog, Integer> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.TYPE, Dialog.Type.PRIVATE);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);

            PreparedQuery<DialogOccupant> preparedQuery = dialogOccupantQueryBuilder.prepare();
            dialogOccupant = dialogOccupantDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogOccupant;
    }

    public DialogOccupant getDialogOccupant(String dialogId, int userId) {
        DialogOccupant dialogOccupant = null;
        try {
            QueryBuilder<DialogOccupant, Integer> queryBuilder = dialogOccupantDao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId).and().eq(User.Column.ID, userId);
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupant = dialogOccupantDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogOccupant;
    }
}