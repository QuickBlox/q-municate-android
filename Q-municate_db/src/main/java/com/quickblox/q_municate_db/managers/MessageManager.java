package com.quickblox.q_municate_db.managers;

import android.os.Handler;
import android.os.Looper;

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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public class MessageManager extends Observable implements CommonDao<Message> {

    public static final String OBSERVE_MESSAGE = "observe_message";
    private static final String TAG = MessageManager.class.getSimpleName();
    private Handler handler;
    private Dao<Message, Integer> messageDao;
    private Dao<Dialog, Integer> dialogDao;
    private Dao<DialogOccupant, Integer> dialogOccupantDao;

    public MessageManager(Dao<Message, Integer> messageDao, Dao<Dialog, Integer> dialogDao, Dao<DialogOccupant, Integer> dialogOccupantDao) {
        handler = new Handler(Looper.getMainLooper());
        this.messageDao = messageDao;
        this.dialogDao = dialogDao;
        this.dialogOccupantDao = dialogOccupantDao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                MessageManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public Dao.CreateOrUpdateStatus createOrUpdate(Message item) {
        Dao.CreateOrUpdateStatus createOrUpdateStatus = null;

        try {
            createOrUpdateStatus = messageDao.createOrUpdate(item);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }

        notifyObservers(OBSERVE_MESSAGE);

        return createOrUpdateStatus;
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

        notifyObservers(OBSERVE_MESSAGE);
    }

    public void createOrUpdate(final Collection<Message> messagesList) {
        try {
            messageDao.callBatchTasks(new Callable<Message>() {
                @Override
                public Message call() throws Exception {
                    for (Message message : messagesList) {
                        messageDao.createOrUpdate(message);
                    }

                    notifyObservers(OBSERVE_MESSAGE);

                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdate() - " + e.getMessage());
        }
    }

    public Message getByMessageId(String messageId) {
        Message message = null;
        try {
            QueryBuilder<Message, Integer> messageQueryBuilder = messageDao.queryBuilder();
            messageQueryBuilder.where().eq(Message.Column.ID, messageId);
            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
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
            queryBuilder.where().in(DialogOccupant.Column.ID, dialogOccupantsList);
            queryBuilder.orderBy(Message.Column.CREATED_DATE, true);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            message = messageDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return message;
    }

    public long getCountUnreadMessages(List<Integer> dialogOccupantsList) {
        long count = 0;
        try {
            QueryBuilder<Message, Integer> queryBuilder = messageDao.queryBuilder();
            queryBuilder.setCountOf(true);
            queryBuilder.where().in(DialogOccupant.Column.ID, dialogOccupantsList).and().eq(
                    Message.Column.STATE, State.DELIVERED);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            count = messageDao.countOf(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return count;
    }

    public List<Message> getMessagesByDialogId(int dialogOccupantId) {
        List<Message> messagesList = null;
        try {
            QueryBuilder<Message, Integer> queryBuilder = messageDao.queryBuilder();
            queryBuilder.where().eq(DialogOccupant.Column.ID, dialogOccupantId);
            queryBuilder.orderBy(Message.Column.CREATED_DATE, true);
            PreparedQuery<Message> preparedQuery = queryBuilder.prepare();
            messagesList = messageDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return messagesList;
    }

    public List<Message> getMessagesByDialogId(String dialogId) {
        List<Message> messagesList = new ArrayList<>();
        try {
            QueryBuilder<Message, Integer> messageQueryBuilder = messageDao.queryBuilder();

            QueryBuilder<DialogOccupant, Integer> dialogOccupantQueryBuilder = dialogOccupantDao.queryBuilder();

            QueryBuilder<Dialog, Integer> dialogQueryBuilder = dialogDao.queryBuilder();
            dialogQueryBuilder.where().eq(Dialog.Column.ID, dialogId);

            dialogOccupantQueryBuilder.join(dialogQueryBuilder);
            messageQueryBuilder.join(dialogOccupantQueryBuilder);

            PreparedQuery<Message> preparedQuery = messageQueryBuilder.prepare();
            messagesList = messageDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return messagesList;
    }
}