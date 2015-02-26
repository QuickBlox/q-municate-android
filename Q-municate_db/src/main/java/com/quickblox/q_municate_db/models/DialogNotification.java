package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = DialogNotification.TABLE_NAME)
public class DialogNotification implements Serializable {

    public static final String TABLE_NAME = "dialog_notification";

    public static final String COLUMN_DIALOG_NOTIFICATION_ID = "dialog_notification_id";
    public static final String COLUMN_CREATED_DATE = "created_date";

    @DatabaseField(id = true, unique = true, columnName = COLUMN_DIALOG_NOTIFICATION_ID)
    private String dialogNotificationId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true,
            columnName = DialogOccupant.COLUMN_DIALOG_OCCUPANT_ID)
    private DialogOccupant dialogOccupant;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = true,
            columnName = Notification.COLUMN_NAME_NOTIFICATION_ID)
    private Notification notification;

    @DatabaseField(columnName = COLUMN_CREATED_DATE)
    private String createdDate;

    public DialogNotification() {
    }

    public DialogNotification(String dialogNotificationId, DialogOccupant dialogOccupant, Notification notification, String createdDate) {
        this.dialogNotificationId = dialogNotificationId;
        this.dialogOccupant = dialogOccupant;
        this.notification = notification;
        this.createdDate = createdDate;
    }

    public String getDialogNotificationId() {
        return dialogNotificationId;
    }

    public void setDialogNotificationId(String dialogNotificationId) {
        this.dialogNotificationId = dialogNotificationId;
    }

    public DialogOccupant getDialogOccupant() {
        return dialogOccupant;
    }

    public void setDialogOccupant(DialogOccupant dialogOccupant) {
        this.dialogOccupant = dialogOccupant;
    }

    public Notification getNotification() {
        return notification;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}