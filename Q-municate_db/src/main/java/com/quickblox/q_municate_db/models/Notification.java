package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Notification.TABLE_NAME)
public class Notification {

    public static final String TABLE_NAME = "notification";

    public static final String COLUMN_NAME_NOTIFICATION_ID = "notification_id";
    public static final String COLUMN_NAME_NOTIFICATION = "notification";

    @DatabaseField(generatedId = true, columnName = COLUMN_NAME_NOTIFICATION_ID)
    private int notificationId;

    @DatabaseField(unique = true, dataType = DataType.ENUM_INTEGER,
            columnName = COLUMN_NAME_NOTIFICATION)
    private Type type;

    public Notification() {
    }

    public Notification(Type type) {
        this.type = type;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        CREATE_PRIVATE_CHAT,
        CREATE_GROUP_CHAT,
        CHANGE_NAME_GROUP_CHAT,
        CHANGE_PHOTO_GROUP_CHAT,
        CHANGE_OCCUPANTS_GROUP_CHAT
    }
}