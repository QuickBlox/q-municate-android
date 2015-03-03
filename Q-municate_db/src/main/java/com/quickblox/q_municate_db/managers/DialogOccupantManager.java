package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class DialogOccupantManager implements CommonDao<DialogOccupant> {

    private Dao<DialogOccupant, Integer> dialogOccupantDao;

    public DialogOccupantManager(Dao<DialogOccupant, Integer> dialogOccupantDao) {
        this.dialogOccupantDao = dialogOccupantDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(DialogOccupant item) {
        try {
            return dialogOccupantDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
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
}