package com.quickblox.q_municate_base_cache;

import android.os.Handler;
import android.os.Looper;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.managers.base.BaseManager;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class QMAbstractBaseCache<T>  implements QMBaseCache<T>{

    private static final String TAG = QMAbstractBaseCache.class.getSimpleName();

    protected Dao<T, Long> dao;

    private Handler handler;

    public QMAbstractBaseCache() {
        handler = new Handler(Looper.getMainLooper());
    }

    public QMAbstractBaseCache(Dao<T, Long> dao) {
        handler = new Handler(Looper.getMainLooper());
        this.dao = dao;
    }


    @Override
    public void create(Object object) {
        try {
            dao.create((T) object);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "create() - " + e.getMessage());
        }
    }


    @Override
    public void createOrUpdate(Object object) {
        try {
            dao.createOrUpdate((T) object);
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
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void deleteById(long id) {
        try {
            dao.deleteById(id);
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

    @Override
    public void clear() {
        DeleteBuilder<T, Long> deleteBuilder = dao.deleteBuilder();
        try {
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}
