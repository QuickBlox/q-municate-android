package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.utils.ErrorUtils;

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
}