package com.quickblox.q_municate_db.managers;

import android.os.Bundle;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class DialogDataManager extends BaseManager<Dialog> {

    private static final String TAG = DialogDataManager.class.getSimpleName();

    public DialogDataManager(Dao<Dialog, Long> dialogDao) {
        super(dialogDao, DialogDataManager.class.getSimpleName());
    }

    public Dialog getByDialogId(String dialogId) {
        Dialog dialog = null;

        try {
            QueryBuilder<Dialog, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ID, dialogId);
            PreparedQuery<Dialog> preparedQuery = queryBuilder.prepare();
            dialog = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialog;
    }

    public Dialog getByRoomJid(String roomJid) {
        Dialog dialog = null;

        try {
            QueryBuilder<Dialog, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(Dialog.Column.ROOM_JID, roomJid);
            PreparedQuery<Dialog> preparedQuery = queryBuilder.prepare();
            dialog = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return dialog;
    }

    public void deleteById(String dialogId) {
        try {
            DeleteBuilder<Dialog, Long> deleteBuilder = dao.deleteBuilder();
            deleteBuilder.where().eq(Dialog.Column.ID, dialogId);

            if (deleteBuilder.delete() > 0){
                //TODO VT need to think how to send ID to observers
                notifyObserversDeletedById(dialogId);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public List<Dialog> getAllSorted() {
        return super.getAllSorted(Dialog.Column.MODIFIED_DATE_LOCAL, false);
    }

    public List<Dialog> getSkippedSorted(int startRow, int perPage) {
        return super.getSkippedSorted(startRow, perPage, Dialog.Column.MODIFIED_DATE_LOCAL, false);
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putString(EXTRA_OBJECT_ID, (String) id);
    }
}