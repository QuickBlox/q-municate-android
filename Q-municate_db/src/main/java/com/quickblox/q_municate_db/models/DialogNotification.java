package com.quickblox.q_municate_db.models;

import android.provider.BaseColumns;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.DialogNotification.Column.CREATED_DATE;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.ID;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.NOTIFICATION_TYPE;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class DialogNotification implements Serializable {

    @DatabaseField(id = true, unique = true, columnName = ID)
    private String dialogNotificationId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true,
            columnName = DialogOccupant.Column.ID)
    private DialogOccupant dialogOccupant;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = true,
            columnName = NOTIFICATION_TYPE)
    private NotificationType notificationType;

    @DatabaseField(columnName = CREATED_DATE)
    private String createdDate;

    public DialogNotification() {
    }

    public DialogNotification(String dialogNotificationId, DialogOccupant dialogOccupant,
            NotificationType notificationType, String createdDate) {
        this.dialogNotificationId = dialogNotificationId;
        this.dialogOccupant = dialogOccupant;
        this.notificationType = notificationType;
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

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType NotificationType) {
        this.notificationType = NotificationType;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public enum NotificationType {

        CREATE_PRIVATE_CHAT(0),
        CREATE_GROUP_CHAT(1),
        CHANGE_NAME_GROUP_CHAT(2),
        CHANGE_PHOTO_GROUP_CHAT(3),
        CHANGE_OCCUPANTS_GROUP_CHAT(4);

        private int code;

        NotificationType(int code) {
            this.code = code;
        }

        public static NotificationType parseByCode(int code) {
            NotificationType[] valuesArray = NotificationType.values();
            NotificationType result = null;
            for (NotificationType value : valuesArray) {
                if (value.getCode() == code) {
                    result = value;
                    break;
                }
            }
            return result;
        }

        public int getCode() {
            return code;
        }
    }

    public interface Column {

        String TABLE_NAME = "dialog_notification";
        String ID = BaseColumns._ID;
        String CREATED_DATE = "created_date";
        String NOTIFICATION_TYPE = "notification_type";
    }
}