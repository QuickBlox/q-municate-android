package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class DialogManager implements CommonDao<Dialog> {

    private Dao<Dialog, Integer> dialogDao;

    public DialogManager(Dao<Dialog, Integer> dialogDao) {
        this.dialogDao = dialogDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Dialog item) {
        try {
            return dialogDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
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
    }

    @Override
    public void delete(Dialog item) {
        try {
            dialogDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Dialog getByDialogId(String dialogId) {
        Dialog dialog = null;
        try {
            QueryBuilder<Dialog, Integer> queryBuilder = dialogDao.queryBuilder();
            queryBuilder.where().eq(Dialog.COLUMN_DIALOG_ID, dialogId);
            PreparedQuery<Dialog> preparedQuery = queryBuilder.prepare();
            dialog = dialogDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialog;
    }
}