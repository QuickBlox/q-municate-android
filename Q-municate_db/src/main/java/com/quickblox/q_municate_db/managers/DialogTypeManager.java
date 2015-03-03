package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.DialogType;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class DialogTypeManager implements CommonDao<DialogType> {

    private Dao<DialogType, Integer> dialogTypeDao;

    public DialogTypeManager(Dao<DialogType, Integer> dialogTypeDao) {
        this.dialogTypeDao = dialogTypeDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(DialogType item) {
        try {
            return dialogTypeDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    @Override
    public List<DialogType> getAll() {
        List<DialogType> dialogTypeList = null;
        try {
            dialogTypeList = dialogTypeDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogTypeList;
    }

    @Override
    public DialogType get(int id) {
        DialogType dialogType = null;
        try {
            dialogType = dialogTypeDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogType;
    }

    @Override
    public void update(DialogType item) {
        try {
            dialogTypeDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(DialogType item) {
        try {
            dialogTypeDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public DialogType getByDialogType(DialogType.Type type) {
        DialogType dialogType = null;
        try {
            QueryBuilder<DialogType, Integer> queryBuilder = dialogTypeDao.queryBuilder();
            queryBuilder.where().eq(DialogType.COLUMN_TYPE, type);
            PreparedQuery<DialogType> preparedQuery = queryBuilder.prepare();
            dialogType = dialogTypeDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return dialogType;
    }
}