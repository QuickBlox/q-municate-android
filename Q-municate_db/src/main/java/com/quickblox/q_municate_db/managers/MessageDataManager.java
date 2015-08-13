package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;

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

    public Message getLastMessageByDialogId(List<Integer> dialogOccupantsList) {
        Message message = null;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(DialogOccupant.Column.ID, dialogOccupantsList);
            queryBuilder.orderBy(Message.Column.CREATED_DATE, true);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = dao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return message;
    }

    public long getCountUnreadMessages(List<Integer> dialogOccupantsList) {
        long count = 0;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.setCountOf(true);
            queryBuilder.where().in(DialogOccupant.Column.ID, dialogOccupantsList).and().eq(
                    Message.Column.STATE, State.DELIVERED);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            count = dao.countOf(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return count;
    }

    public List<Message> getMessagesByDialogId(int dialogOccupantId) {
        List<Message> messagesList = null;

        try {
            QueryBuilder<Message, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(DialogOccupant.Column.ID, dialogOccupantId);
            queryBuilder.orderBy(Message.Column.CREATED_DATE, true);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            messagesList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return messagesList;
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
}