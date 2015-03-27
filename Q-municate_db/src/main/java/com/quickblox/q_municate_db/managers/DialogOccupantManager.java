package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

public class DialogOccupantManager implements CommonDao<DialogOccupant> {

    private static final String TAG = DialogOccupantManager.class.getSimpleName();

    private Dao<DialogOccupant, Integer> dialogOccupantDao;

    public DialogOccupantManager(Dao<DialogOccupant, Integer> dialogOccupantDao) {
        this.dialogOccupantDao = dialogOccupantDao;
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

    public List<DialogOccupant> getDialogOccupantsListByDialog(String dialogId) {
        List<DialogOccupant> dialogOccupant = null;
        try {
            QueryBuilder<DialogOccupant, Integer> queryBuilder = dialogOccupantDao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId);
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupant = dialogOccupantDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogOccupant;
    }
}