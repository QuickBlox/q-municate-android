package com.quickblox.q_municate_db.helpers;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.AttachmentType;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.DialogType;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.Notification;
import com.quickblox.q_municate_db.models.Role;
import com.quickblox.q_municate_db.models.Social;
import com.quickblox.q_municate_db.models.SocialType;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.Status;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.models.UserRequest;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.util.concurrent.ConcurrentHashMap;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "q_municate_db.sqlite";

    private static final int DATABASE_VERSION = 30;

    private ConcurrentHashMap<Class<?>, Dao> concurrentDaoHashMap = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        concurrentDaoHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, User.class);
            TableUtils.createTable(connectionSource, Friend.class);
            TableUtils.createTable(connectionSource, Role.class);
            TableUtils.createTable(connectionSource, SocialType.class);
            TableUtils.createTable(connectionSource, Social.class);
            TableUtils.createTable(connectionSource, Status.class);
            TableUtils.createTable(connectionSource, UserRequest.class);
            TableUtils.createTable(connectionSource, DialogType.class);
            TableUtils.createTable(connectionSource, Dialog.class);
            TableUtils.createTable(connectionSource, DialogOccupant.class);
            TableUtils.createTable(connectionSource, DialogNotification.class);
            TableUtils.createTable(connectionSource, Notification.class);
            TableUtils.createTable(connectionSource, AttachmentType.class);
            TableUtils.createTable(connectionSource, Attachment.class);
            TableUtils.createTable(connectionSource, State.class);
            TableUtils.createTable(connectionSource, Message.class);
        } catch (java.sql.SQLException e) {
            ErrorUtils.logError(e);
        }

        // TODO TEMP
        TablesInitHelper.init();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
            int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, User.class, true);
            TableUtils.dropTable(connectionSource, Friend.class, true);
            TableUtils.dropTable(connectionSource, Role.class, true);
            TableUtils.dropTable(connectionSource, SocialType.class, true);
            TableUtils.dropTable(connectionSource, Social.class, true);
            TableUtils.dropTable(connectionSource, Status.class, true);
            TableUtils.dropTable(connectionSource, UserRequest.class, true);
            TableUtils.dropTable(connectionSource, DialogType.class, true);
            TableUtils.dropTable(connectionSource, Dialog.class, true);
            TableUtils.dropTable(connectionSource, DialogOccupant.class, true);
            TableUtils.dropTable(connectionSource, DialogNotification.class, true);
            TableUtils.dropTable(connectionSource, Notification.class, true);
            TableUtils.dropTable(connectionSource, AttachmentType.class, true);
            TableUtils.dropTable(connectionSource, Attachment.class, true);
            TableUtils.dropTable(connectionSource, State.class, true);
            TableUtils.dropTable(connectionSource, Message.class, true);
        } catch (java.sql.SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public void clearTables() {
        try {
            TableUtils.clearTable(connectionSource, User.class);
            TableUtils.clearTable(connectionSource, Friend.class);
            TableUtils.clearTable(connectionSource, Social.class);
            TableUtils.clearTable(connectionSource, UserRequest.class);
            TableUtils.clearTable(connectionSource, Dialog.class);
            TableUtils.clearTable(connectionSource, DialogOccupant.class);
            TableUtils.clearTable(connectionSource, DialogNotification.class);
            TableUtils.clearTable(connectionSource, Attachment.class);
            TableUtils.clearTable(connectionSource, Message.class);
        } catch (java.sql.SQLException e) {
            ErrorUtils.logError(e);
        }
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