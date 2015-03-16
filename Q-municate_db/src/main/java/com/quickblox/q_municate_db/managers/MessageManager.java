package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class MessageManager implements CommonDao<Message> {

    private static final String TAG = MessageManager.class.getSimpleName();

    private Dao<Message, Integer> messageDao;

    public MessageManager(Dao<Message, Integer> messageDao) {
        this.messageDao = messageDao;
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Message item) {
        try {
            return messageDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<Message> getAll() {
        List<Message> messageList = null;
        try {
            messageList = messageDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return messageList;
    }

    @Override
    public Message get(int id) {
        Message message = null;
        try {
            message = messageDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return message;
    }

    @Override
    public void update(Message item) {
        try {
            messageDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Message item) {
        try {
            messageDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Message getByMessageId(String messageId) {
        Message message = null;
        try {
            QueryBuilder<Message, Integer> queryBuilder = messageDao.queryBuilder();
            queryBuilder.where().eq(Message.COLUMN_MESSAGE_ID, messageId);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = messageDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return message;
    }

    public Message getLastMessageByDialogId(List<Integer> dialogOccupantsList) {
        Message message = null;
        try {
            QueryBuilder<Message, Integer> queryBuilder = messageDao.queryBuilder();
            queryBuilder.where().in(DialogOccupant.COLUMN_DIALOG_OCCUPANT_ID, dialogOccupantsList);
            queryBuilder.orderBy(Message.COLUMN_CREATED_DATE, true);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = messageDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return message;
    }

    public long getCountUnreadMessages(List<Integer> dialogOccupantsList, State state) {
        long count = 0;
        try {
            QueryBuilder<Message, Integer> queryBuilder = messageDao.queryBuilder();
            queryBuilder.setCountOf(true);
            queryBuilder.where().in(DialogOccupant.COLUMN_DIALOG_OCCUPANT_ID, dialogOccupantsList);
            queryBuilder.where().eq(State.COLUMN_STATE_ID, state.getStateId());
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            count = messageDao.countOf(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return count;
    }
}