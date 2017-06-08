package com.quickblox.q_municate_base_cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.quickblox.q_municate_base_cache.utils.DbHelperUtils;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;

import java.util.concurrent.ConcurrentHashMap;

public abstract class QMBaseDataHelper extends OrmLiteSqliteOpenHelper {

    private ConcurrentHashMap<Class<?>, Dao> concurrentDaoHashMap = null;

    public QMBaseDataHelper(Context context, String databaseName, int databaseVersion, int configFileId) {
        super(context, databaseName, null, databaseVersion, configFileId);
        concurrentDaoHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        DbHelperUtils.onCreate(connectionSource, getTables());
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        DbHelperUtils.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        DbHelperUtils.onUpgrade(database, connectionSource, getTables());
    }

    public void clearTable(Class clazz) {
        DbHelperUtils.clearTable(connectionSource, clazz);
    }

    public void clearTables() {
        DbHelperUtils.clearTables(connectionSource, getTables());
    }

    public <T> Dao getDaoByClass(Class<T> clazz) {
        try {
            if (isInMap(clazz)) {
                return getFromMap(clazz);
            } else {
                return addToMap(clazz, getDao(clazz));
            }
        } catch (java.sql.SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    private <T> Dao addToMap(Class<T> clazz, Dao dao) {
        concurrentDaoHashMap.put(clazz, dao);
        return concurrentDaoHashMap.put(clazz, dao);
    }

    private <T> boolean isInMap(Class<T> clazz) {
        return concurrentDaoHashMap.contains(clazz);
    }

    public <T> Dao getFromMap(Class<T> clazz) {
        return concurrentDaoHashMap.get(clazz);
    }


    protected abstract Class<?>[] getTables();


}
