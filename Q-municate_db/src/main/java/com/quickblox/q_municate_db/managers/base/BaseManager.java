package com.quickblox.q_municate_db.managers.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public abstract class BaseManager<T> extends Observable implements Manager {

    public static final int CREATE_ACTION = 1;
    public static final int CREATE_OR_UPDATE_ACTION = 2;
    public static final int CREATE_OR_UPDATE_ALL_ACTION = 3;

    public static final int UPDATE_ACTION = 4;
    public static final int UPDATE_ALL_ACTION = 5;

    public static final int DELETE_ACTION = 6;
    public static final int DELETE_BY_ID_ACTION = 7;

    public static final String EXTRA_OBJECT = "extra_object";
    public static final String EXTRA_ACTION = "extra_action";
    public static final String EXTRA_OBSERVE_KEY = "extra_observe_key";
    public static final String EXTRA_OBJECT_ID = "extra_object_id";

    private String observeKey;

    private static final String TAG = BaseManager.class.getSimpleName();

    protected Dao<T, Long> dao;

    private Handler handler;

    public BaseManager(Dao<T, Long> dao, String observeKey) {
        this.observeKey = observeKey;
        handler = new Handler(Looper.getMainLooper());
        this.dao = dao;
    }

    public void notifyObservers(final T data, final int action){
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                BaseManager.super.notifyObservers(getDataToNotify(data, action));
            }
        });
    }

    public void notifyObserversDeletedById(final Object id){
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                BaseManager.super.notifyObservers(getDataToNotifyDeletedById(id));
            }
        });
    }

    @Override
    public void create(Object object) {
        try {
            dao.create((T) object);

            notifyObservers((T)object, CREATE_ACTION);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "create() - " + e.getMessage());
        }
    }

    @Override
    public void createOrUpdate(Object object) {
        createOrUpdate(object, true);
    }

    @Override
    public void createOrUpdate(Object object, boolean notify) {
        try {
            Dao.CreateOrUpdateStatus status = dao.createOrUpdate((T) object);

            if (notify) {
                notifyObservers((T) object, status.isCreated() ? CREATE_ACTION : UPDATE_ACTION);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdate(Object) - " + e.getMessage());
        }
    }

    @Override
    public void createOrUpdateAll(final Collection objectsCollection) {
        try {
            dao.callBatchTasks(new Callable() {
                @Override
                public T call() throws Exception {
                    for (Object object : objectsCollection) {
                        createOrUpdate(object, false);
                    }

                    notifyObservers(null, CREATE_OR_UPDATE_ALL_ACTION);
                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdateAll(Collection) - " + e.getMessage());
        }
    }

    @Override
    public T get(long id) {
        try {
            return dao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return null;
    }

    @Override
    public List<T> getAll() {
        try {
            return dao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<T> getAllSorted(String sortedColumn, boolean ascending) {
        List<T> objectsList = Collections.emptyList();

        try {
            QueryBuilder<T, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy(sortedColumn, ascending);
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            objectsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return objectsList;
    }


    public List<T> getSkippedSorted(long startRow, long perPage, String sortedColumn, boolean ascending) {
        List<T> objectsList = Collections.emptyList();

        try {
            QueryBuilder<T, Long> queryBuilder = dao.queryBuilder();
            queryBuilder.offset(startRow).limit(perPage);
            queryBuilder.orderBy(sortedColumn, ascending);
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            objectsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return objectsList;
    }

    public long getAllCount() {
        long numRows = 0;
        try {
            numRows = dao.countOf();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return numRows;
    }

    @Override
    public void update(Object object) {
        update(object, true);
    }

    @Override
    public void update(Object object, boolean notify) {
        try {
            dao.update((T) object);

            if (notify) {
                notifyObservers((T)object, UPDATE_ACTION);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void updateAll(final Collection objectsCollection) {
        try {
            dao.callBatchTasks(new Callable() {
                @Override
                public T call() throws Exception {
                    for (Object object : objectsCollection) {
                        update(object, false);
                    }
                    notifyObservers(null, UPDATE_ALL_ACTION);

                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "updateAll(Collection) - " + e.getMessage());
        }
    }

    @Override
    public void delete(Object object) {
        try {
            dao.delete((T) object);

            notifyObservers((T)object, DELETE_ACTION);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    //TODO VT this method never used. Need review necessity this method
    @Override
    public void deleteById(long id) {
        try {
            dao.deleteById(id);

            notifyObserversDeletedById(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public boolean exists(long id) {
        try {
            return dao.idExists(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return false;
    }

    public String getObserverKey(){
        return observeKey;
    }

    protected Object getDataToNotify(T data, int action){
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_OBJECT, (Serializable) data);
        bundle.putInt(EXTRA_ACTION, action);
        bundle.putString(EXTRA_OBSERVE_KEY, getObserverKey());
        return bundle;
    }

    protected Object getDataToNotifyDeletedById(Object id){
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_ACTION, DELETE_BY_ID_ACTION);
        bundle.putString(EXTRA_OBSERVE_KEY, getObserverKey());
        addIdToNotification(bundle, id);
        return bundle;
    }

    abstract protected void addIdToNotification(Bundle bundle, Object id);

    protected EventObject<T> getEventToNotify(T data, int action){
        return new EventObject<T>(data, action, getObserverKey());
    }

    public static class EventObject<T>{
        private T data;
        private int action;
        private String observerKey;

        public EventObject(T data, int action, String observerKey) {
            this.data = data;
            this.action = action;
            this.observerKey = observerKey;
        }

        public T getData() {
            return data;
        }

        public int getAction() {
            return action;
        }

        public String getObserverKey() {
            return observerKey;
        }
    }
}