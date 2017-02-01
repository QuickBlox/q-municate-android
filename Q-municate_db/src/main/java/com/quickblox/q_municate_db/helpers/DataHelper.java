package com.quickblox.q_municate_db.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.quickblox.q_municate_db.R;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_db.utils.DbHelperUtils;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.concurrent.ConcurrentHashMap;

public class DataHelper extends OrmLiteSqliteOpenHelper {

    private ConcurrentHashMap<Class<?>, Dao> concurrentDaoHashMap = null;

    public static final Class<?>[] TABLES = {
            QMUser.class,
            Social.class,
            Friend.class,
            UserRequest.class,
            Dialog.class,
            DialogOccupant.class,
            DialogNotification.class,
            Attachment.class,
            Message.class
    };

    public DataHelper(Context context) {
        super(context, context.getString(R.string.db_name), null, context.getResources().getInteger(
                R.integer.db_version), R.raw.orm);
        concurrentDaoHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        DbHelperUtils.onCreate(connectionSource, TABLES);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        DbHelperUtils.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        DbHelperUtils.onUpgrade(database, connectionSource, TABLES);
    }

    public void clearTable(Class clazz) {
        DbHelperUtils.clearTable(connectionSource, clazz);
    }

    public void clearTables() {
        DbHelperUtils.clearTables(connectionSource, TABLES);
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
}