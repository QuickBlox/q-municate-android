package com.quickblox.q_municate_db.managers;

import android.os.Bundle;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MessageDataManager extends BaseManager<Message> {

    private static final String TAG = MessageDataManager.class.getSimpleName();

    private Dao<Dialog, Long> dialogDao;
    private Dao<DialogOccupant, Long> dialogOccupantDao;

    public MessageDataManager(Dao<Message, Long> messageDao, Dao<Dialog, Long> dialogDao,
            Dao<DialogOccupant, Long> dialogOccupantDao) {
        super(messageDao, MessageDataManager.class.getSimpleName());
        this.dialogDao = dialogDao;
        this.dialogOccupantDao = dialogOccupantDao;
    }

    public Message getByMessageId(String messageId) {
        Message message = null;

        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();
            messageQueryBuilder.where().eq(Message.Column.ID, messageId);
            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return message;
    }

    public Message getLastMessageWithTempByDialogId(List<Long> dialogOccupantsList) {
        Message message = null;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(DialogOccupant.Column.ID, dialogOccupantsList);
            queryBuilder.orderBy(Message.Column.CREATED_DATE, false);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return message;
    }

    public Message getMessageByDialogId(boolean firstMessage, List<Long> dialogOccupantsList) {
        Message message = null;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            Where<Message, Long> where = queryBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsList),
                    where.eq(Message.Column.STATE, State.READ)
            );
            queryBuilder.orderBy(Message.Column.CREATED_DATE, firstMessage);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return message;
    }

    public long getCountUnreadMessages(List<Long> dialogOccupantsIdsList, int currentUserId) {
        long count = 0;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.setCountOf(true);

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();
            dialogOccupantQueryBuilder.where().ne(QMUserColumns.ID, currentUserId);

            queryBuilder.join(dialogOccupantQueryBuilder);

            Where<Message, Long> where = queryBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsIdsList),
                    where.or(
                            where.eq(Message.Column.STATE, State.DELIVERED),
                            where.eq(Message.Column.STATE, State.TEMP_LOCAL_UNREAD)
                    )
            );

            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            count = dao.countOf(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return count;
    }

    public List<Message> getUnreadMessages(List<Long> dialogOccupantsIdsList, int currentUserId) {

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();
            dialogOccupantQueryBuilder.where().ne(QMUserColumns.ID, currentUserId);

            queryBuilder.join(dialogOccupantQueryBuilder);

            Where<Message, Long> where = queryBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsIdsList),
                    where.or(
                            where.eq(Message.Column.STATE, State.DELIVERED),
                            where.eq(Message.Column.STATE, State.TEMP_LOCAL_UNREAD)
                    )
            );

            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            return dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return null;
    }

    public Message getLastMessageByDialogId(List<Long> dialogOccupantsList) {
        Message message = null;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            Where<Message, Long> where = queryBuilder.where();
            where.in(DialogOccupant.Column.ID, dialogOccupantsList);
            queryBuilder.orderBy(Message.Column.CREATED_DATE, false);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return message;
    }

    public List<Message> getMessagesByDialogId(String dialogId) {
        List<Message> messagesList = new ArrayList<>();

        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return messagesList;
    }

    public List<Message> getMessagesByDialogIdLimeted(String dialogId, long limit) {
        List<Message> messagesList = new ArrayList<>();

        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder
                    .join(dialogOccupantQueryBuilder)
                    .orderBy(Message.Column.CREATED_DATE, false)
                    .limit(limit);

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return messagesList;
    }

    public List<Message> getMessagesByDialogIdAndDate(String dialogId, long createdDate, boolean moreDate){
        List<Message> messagesList = new ArrayList<>();

        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();

            Where<Message, Long> where = messageQueryBuilder.where();
            where.and(where.ne(Message.Column.STATE, State.TEMP_LOCAL),
                    where.ne(Message.Column.STATE, State.TEMP_LOCAL_UNREAD),
                    moreDate
                            ? where.gt(Message.Column.CREATED_DATE, createdDate)
                            : where.lt(Message.Column.CREATED_DATE, createdDate));

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return messagesList;
    }

    public List<Message> getMessagesByDialogIdAndDate(String dialogId, long createdDate, boolean moreDate, long limit){
        List<Message> messagesList = new ArrayList<>();

        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();

            Where<Message, Long> where = messageQueryBuilder.where();
            where.and(where.ne(Message.Column.STATE, State.TEMP_LOCAL),
                    where.ne(Message.Column.STATE, State.TEMP_LOCAL_UNREAD),
                    moreDate
                            ? where.gt(Message.Column.CREATED_DATE, createdDate)
                            : where.lt(Message.Column.CREATED_DATE, createdDate));

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder
                    .join(dialogOccupantQueryBuilder)
                    .orderBy(Message.Column.CREATED_DATE, false)
                    .limit(limit);

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return messagesList;
    }

    public void deleteTempMessages(List<Long> dialogOccupantsIdsList) {
        try {
            DeleteBuilder<Message, Long> deleteBuilder = dao.deleteBuilder();

            Where<Message, Long> where = deleteBuilder.where();
            where.and(
                    where.in(DialogOccupant.Column.ID, dialogOccupantsIdsList),
                    where.or(
                            where.eq(Message.Column.STATE, State.TEMP_LOCAL),
                            where.eq(Message.Column.STATE, State.TEMP_LOCAL_UNREAD)
                    )
            );

            if (deleteBuilder.delete() > 0) {
                //TODO VT need to think how to send IDs to observers
                notifyObserversDeletedById(dialogOccupantsIdsList);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

    }

    public List<Message> getTempMessagesByDialogId(String dialogId){
        List<Message> messagesList = new ArrayList<>();
        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            Where<Message, Long> where = messageQueryBuilder.where();
            where.or(where.eq(Message.Column.STATE, State.TEMP_LOCAL),
                    where.eq(Message.Column.STATE, State.TEMP_LOCAL_UNREAD));

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messagesList;
    }

    public Message getLastTempMessagesByDialogId(String dialogId){
        Message message = null;
        try {
            QueryBuilder<Message, Long> messageQueryBuilder = dao.queryBuilder();

            QueryBuilder<DialogOccupant, Long> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Long> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            Where<Message, Long> where = messageQueryBuilder.where();
            where.or(where.eq(Message.Column.STATE, State.TEMP_LOCAL),
                    where.eq(Message.Column.STATE, State.TEMP_LOCAL_UNREAD));
            messageQueryBuilder.orderBy(Message.Column.CREATED_DATE, false);

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return message;
    }

    @Override
    protected void addIdToNotification(Bundle bundle, Object id) {
        bundle.putSerializable(EXTRA_OBJECT_ID, (Serializable) id);
    }
}