package com.quickblox.q_municate_db.managers;

import android.os.Bundle;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DialogOccupantDataManager extends BaseManager<DialogOccupant> {

    private static final String TAG = DialogOccupantDataManager.class.getSimpleName();

    private Dao<Dialog, Long> dialogDao;

    public DialogOccupantDataManager(Dao<DialogOccupant, Long> dialogOccupantDao,
            Dao<Dialog, Long> dialogDao) {
        super(dialogOccupantDao, DialogOccupantDataManager.class.getSimpleName());
        this.dialogDao = dialogDao;
    }

    @Override
    public void createOrUpdate(Object object, boolean notify) {
        DialogOccupant dialogOccupant = (DialogOccupant) object;
        try {
            int action;

            if(dialogOccupant.getDialog() != null && existsByDialogIdAndUserId(dialogOccupant.getDialog().getDialogId(), dialogOccupant.getUser().getId())){
                dao.update(dialogOccupant);
                action = UPDATE_ACTION;
            } else{
                dao.create(dialogOccupant);
                action = CREATE_ACTION;
            }

            if (notify) {
                notifyObservers(dialogOccupant, action);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate(DialogOccupant) - " + e.getMessage());
        }
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putLong(EXTRA_OBJECT_ID, (Long) id);
    }

    public List<DialogOccupant> getDialogOccupantsListByDialogId(String dialogId) {
        List<DialogOccupant> dialogOccupantsList = null;

        try {
            QueryBuilder<DialogOccupant, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId);
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupantsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupantsList;
    }

    public DialogOccupant getDialogOccupantForPrivateChat(int userId) {
        DialogOccupant dialogOccupant = null;

        try {
            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dao.queryBuilder();
            dialogOccupantQueryBuilder.where().eq(QMUserColumns.ID, userId);

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.TYPE, Dialog.Type.PRIVATE);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);

            PreparedQuery<DialogOccupant> preparedQuery = dialogOccupantQueryBuilder.prepare();
            dialogOccupant = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupant;
    }

    public DialogOccupant getDialogOccupant(String dialogId, int userId) {
        DialogOccupant dialogOccupant = null;

        try {
            QueryBuilder<DialogOccupant, Long> queryBuilder = dao.queryBuilder();
            Where<DialogOccupant, Long> where = queryBuilder.where();
            where.and(where.eq(Dialog.Column.ID, dialogId), where.eq(QMUserColumns.ID, userId));
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupant = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupant;
    }

    public List<DialogOccupant> getActualDialogOccupantsByIds(String dialogId, List<Integer> userIdsList) {
        List<DialogOccupant> dialogOccupantsList = Collections.emptyList();

        try {
            QueryBuilder<DialogOccupant, Long> queryBuilder = dao.queryBuilder();
            Where<DialogOccupant, Long> where = queryBuilder.where();
            where.and(
                    where.in(QMUserColumns.ID, userIdsList),
                    where.eq(DialogOccupant.Column.STATUS, DialogOccupant.Status.ACTUAL),
                    where.eq(Dialog.Column.ID, dialogId)
            );
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupantsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupantsList;
    }

    public List<DialogOccupant> getActualDialogOccupantsByDialog(String dialogId) {
        List<DialogOccupant> dialogOccupantsList = Collections.emptyList();

        try {
            QueryBuilder<DialogOccupant, Long> queryBuilder = dao.queryBuilder();
            Where<DialogOccupant, Long> where = queryBuilder.where();
            where.and(
                    where.eq(DialogOccupant.Column.STATUS, DialogOccupant.Status.ACTUAL),
                    where.eq(Dialog.Column.ID, dialogId)
            );
            PreparedQuery<DialogOccupant> preparedQuery = queryBuilder.prepare();
            dialogOccupantsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogOccupantsList;
    }

    public boolean existsByDialogIdAndUserId(String dialogId, int userId) {
        return getDialogOccupant(dialogId, userId) != null;
    }
}