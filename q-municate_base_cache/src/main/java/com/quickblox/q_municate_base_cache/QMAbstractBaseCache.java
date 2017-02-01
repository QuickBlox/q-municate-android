package com.quickblox.q_municate_base_cache;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;

public abstract class QMAbstractBaseCache<T, ID>  extends Observable implements QMBaseCache<T, ID>{

    private static final String TAG = QMAbstractBaseCache.class.getSimpleName();

    public static String OBSERVE_KEY;

    protected Dao<T, ID> dao;

    private Handler handler;

    public QMAbstractBaseCache() {
        handler = new Handler(Looper.getMainLooper());
    }

    public void notifyObservers(final Object data) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                setChanged();
                QMAbstractBaseCache.super.notifyObservers(data);
            }
        });
    }

    @Override
    public void create(T object) {
        try {
            dao.create(object);
            notifyObservers(OBSERVE_KEY);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "create() - " + e.getMessage());
        }
    }


    @Override
    public void createOrUpdate(Object object) {
        try {
            dao.createOrUpdate((T) object);
            notifyObservers(OBSERVE_KEY);
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
                        createOrUpdate(object);
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
    public T get(ID id) {
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
            QueryBuilder<T, ID> queryBuilder = dao.queryBuilder();
            queryBuilder.orderBy(sortedColumn, ascending);
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            objectsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return objectsList;
    }

    @Override
    public List<T> getByColumn(String column, String value) {
        List<T> objectsList  = Collections.emptyList();

        try {
            QueryBuilder<T, ID> queryBuilder = dao.queryBuilder();
            queryBuilder.where().eq(column, value);
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            objectsList= dao.query(preparedQuery);
        } catch (SQLException e) {
            com.quickblox.q_municate_base_cache.utils.ErrorUtils.logError(e);
        }

        return objectsList;
    }

    @Override
    public List<T> getByColumn(String column, Collection<String> values) {
        List<T> objectsList  = Collections.emptyList();

        try {
            QueryBuilder<T, ID> queryBuilder = dao.queryBuilder();
            queryBuilder.where().in(column, values);
            PreparedQuery<T> preparedQuery = queryBuilder.prepare();
            objectsList = dao.query(preparedQuery);
        } catch (SQLException e) {
            com.quickblox.q_municate_base_cache.utils.ErrorUtils.logError(e);
        }

        return objectsList;
    }


    @Override
    public void update(Object object) {
        try {
            dao.update((T) object);
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
                        update(object);
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
    public void deleteById(ID id) {
        try {
            dao.deleteById(id);
            notifyObservers(OBSERVE_KEY);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public boolean exists(ID id) {
        try {
            return dao.idExists(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return false;
    }

    @Override
    public void clear() {
        DeleteBuilder<T, ID> deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}
