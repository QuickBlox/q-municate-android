package com.quickblox.q_municate_db.managers.base;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public abstract class BaseManager<T> extends Observable implements Manager {

    public static String OBSERVE_KEY;

    private static final String TAG = BaseManager.class.getSimpleName();

    protected Dao<T, Long> dao;

    private Handler handler;

    public BaseManager(Dao<T, Long> dao, String observeKey) {
        OBSERVE_KEY = observeKey;
        handler = new Handler(Looper.getMainLooper());
        this.dao = dao;
    }

    @Override
    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                BaseManager.super.notifyObservers(data);
            }
        });
    }

    @Override
    public void create(Object object) {
        try {
            dao.create((T) object);

            notifyObservers(OBSERVE_KEY);
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
            dao.createOrUpdate((T) object);

            if (notify) {
                notifyObservers(OBSERVE_KEY);
            }
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdateAll(Object) - " + e.getMessage());
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

                    notifyObservers(OBSERVE_KEY);

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

    @Override
    public void update(Object object) {
        update(object, true);
    }

    @Override
    public void update(Object object, boolean notify) {
        try {
            dao.update((T) object);

            if (notify) {
                notifyObservers(OBSERVE_KEY);
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

                    notifyObservers(OBSERVE_KEY);

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

            notifyObservers(OBSERVE_KEY);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            dao.deleteById(id);

            notifyObservers(OBSERVE_KEY);
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
}