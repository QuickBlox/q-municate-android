package com.quickblox.q_municate_db.managers;

import android.os.Bundle;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DialogNotificationDataManager extends BaseManager<DialogNotification> {

    private static final String TAG = DialogNotificationDataManager.class.getSimpleName();

    private Dao<Dialog, Long> dialogDao;
    private Dao<DialogOccupant, Long> dialogOccupantDao;

    public DialogNotificationDataManager(Dao<DialogNotification, Long> dialogNotificationDao,
            Dao<Dialog, Long> dialogDao, Dao<DialogOccupant, Long> dialogOccupantDao) {
        super(dialogNotificationDao, DialogNotificationDataManager.class.getSimpleName());
        this.dialogDao = dialogDao;
        this.dialogOccupantDao = dialogOccupantDao;
    }

    public List<DialogNotification> getDialogNotificationsByDialogId(String dialogId) {
        List<DialogNotification> dialogNotificationsList = new ArrayList<>();

        try {
            QueryBuilder<DialogNotification, Long> messageQueryBuilder = dao
                    .queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao
                    .queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            PreparedQuery<DialogNotification> preparedQuery = messageQueryBuilder.prepare();
            dialogNotificationsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotificationsList;
    }

    public List<DialogNotification> getDialogNotificationsByDialogId(String dialogId, long limit) {
        List<DialogNotification> dialogNotificationsList = new ArrayList<>();

        try {
            QueryBuilder<DialogNotification, Long> messageQueryBuilder = dao
                    .queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao
                    .queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder
                    .join(dialogOccupantQueryBuilder)
                    .orderBy(DialogNotification.Column.CREATED_DATE, false)
                    .limit(limit);

            PreparedQuery<DialogNotification> preparedQuery = messageQueryBuilder.prepare();
            dialogNotificationsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotificationsList;
    }

    public List<DialogNotification> getDialogNotificationsByDialogIdAndDate(String dialogId, long createdDate, boolean moreDate) {
        List<DialogNotification> dialogNotificationsList = new ArrayList<>();

        try {
            QueryBuilder<DialogNotification, Long> messageQueryBuilder = dao.queryBuilder();

            Where<DialogNotification, Long> where = messageQueryBuilder.where();
            where.and(where.ne(DialogNotification.Column.STATE, State.TEMP_LOCAL),
                    where.ne(DialogNotification.Column.STATE, State.TEMP_LOCAL_UNREAD),
                    moreDate
                            ? where.gt(DialogNotification.Column.CREATED_DATE, createdDate)
                            : where.lt(DialogNotification.Column.CREATED_DATE, createdDate));

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao
                    .queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            PreparedQuery<DialogNotification> preparedQuery = messageQueryBuilder.prepare();
            dialogNotificationsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotificationsList;
    }

    public List<DialogNotification> getDialogNotificationsByDialogIdAndDate(String dialogId, long createdDate, boolean moreDate, long limit) {
        List<DialogNotification> dialogNotificationsList = new ArrayList<>();

        try {
            QueryBuilder<DialogNotification, Long> messageQueryBuilder = dao.queryBuilder();

            Where<DialogNotification, Long> where = messageQueryBuilder.where();
            where.and(where.ne(DialogNotification.Column.STATE, State.TEMP_LOCAL),
                    where.ne(DialogNotification.Column.STATE, State.TEMP_LOCAL_UNREAD),
                    moreDate
                            ? where.gt(DialogNotification.Column.CREATED_DATE, createdDate)
                            : where.lt(DialogNotification.Column.CREATED_DATE, createdDate));

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao
                    .queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder
                    .join(dialogOccupantQueryBuilder)
                    .orderBy(DialogNotification.Column.CREATED_DATE, false)
                    .limit(limit);

            PreparedQuery<DialogNotification> preparedQuery = messageQueryBuilder.prepare();
            dialogNotificationsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotificationsList;
    }

    public DialogNotification getLastDialogNotificationByDialogId(List<Long> dialogOccupantsList) {
        DialogNotification dialogNotification = null;

        try {
            QueryBuilder<DialogNotification, Long> queryBuilder = dao.queryBuilder();
            Where<DialogNotification, Long> where = queryBuilder.where();
            where.in(DialogOccupant.Column.ID, dialogOccupantsList);
            queryBuilder.orderBy(DialogNotification.Column.CREATED_DATE, false);
            PreparedQuery<DialogNotification> preparedQuery = queryBuilder.prepare();
            dialogNotification = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotification;
    }

    public DialogNotification getDialogNotificationByDialogId(boolean firstMessage, List<Long> dialogOccupantsList) {
        DialogNotification dialogNotification = null;

        try {
            QueryBuilder<DialogNotification, Long> queryBuilder = dao.queryBuilder();
            Where<DialogNotification, Long> where = queryBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsList),
                    where.eq(DialogNotification.Column.STATE, State.READ)
            );
            queryBuilder.orderBy(DialogNotification.Column.CREATED_DATE, firstMessage);
            PreparedQuery<DialogNotification> preparedQuery = queryBuilder.prepare();
            dialogNotification = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotification;
    }

    public long getCountUnreadDialogNotifications(List<Long> dialogOccupantsIdsList, int currentUserId) {
        long count = 0;

        try {
            QueryBuilder<DialogNotification, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.setCountOf(true);

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();
            dialogOccupantQueryBuilder.where().ne(QMUserColumns.ID, currentUserId);

            queryBuilder.join(dialogOccupantQueryBuilder);

            Where<DialogNotification, Long> where = queryBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsIdsList),
                    where.or(
                            where.eq(DialogNotification.Column.STATE, State.DELIVERED),
                            where.eq(DialogNotification.Column.STATE, State.TEMP_LOCAL_UNREAD)
                    )
            );

            PreparedQuery<DialogNotification> preparedQuery = queryBuilder.prepare();
            count = dao.countOf(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return count;
    }

    public List<DialogNotification> getUnreadDialogNotifications(List<Long> dialogOccupantsIdsList, int currentUserId) {
        long count = 0;

        List<DialogNotification> dialogNotificationsList = null;

        try {
            QueryBuilder<DialogNotification, Long> queryBuilder = dao.queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();
            dialogOccupantQueryBuilder.where().ne(QMUserColumns.ID, currentUserId);

            queryBuilder.join(dialogOccupantQueryBuilder);

            Where<DialogNotification, Long> where = queryBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsIdsList),
                    where.or(
                            where.eq(DialogNotification.Column.STATE, State.DELIVERED),
                            where.eq(DialogNotification.Column.STATE, State.TEMP_LOCAL_UNREAD)
                    )
            );

            PreparedQuery<DialogNotification> preparedQuery = queryBuilder.prepare();
            Log.i(TAG, "query=" + preparedQuery.getStatement());
            dialogNotificationsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialogNotificationsList;
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putString(EXTRA_OBJECT_ID, (String) id);
    }
}