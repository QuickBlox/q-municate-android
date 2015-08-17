package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.DialogNotification.Column.BODY;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.CREATED_DATE;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.ID;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.NOTIFICATION_TYPE;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.STATE;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class DialogNotification implements Serializable {

    @DatabaseField(
            id = true,
            unique = true,
            columnName = ID)
    private String dialogNotificationId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            columnName = DialogOccupant.Column.ID)
    private DialogOccupant dialogOccupant;

    @DatabaseField(
            columnName = STATE)
    private State state;

    @DatabaseField(
            columnName = NOTIFICATION_TYPE)
    private NotificationType notificationType;

    @DatabaseField(
            columnName = BODY)
    private String body;

    @DatabaseField(
            columnName = CREATED_DATE)
    private long createdDate;

    public DialogNotification() {
    }

    public DialogNotification(String dialogNotificationId, DialogOccupant dialogOccupant, State state,
            NotificationType notificationType, long createdDate) {
        this.dialogNotificationId = dialogNotificationId;
        this.dialogOccupant = dialogOccupant;
        this.state = state;
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum NotificationType {

        FRIENDS_REQUEST(4), FRIENDS_ACCEPT(5), FRIENDS_REJECT(6), FRIENDS_REMOVE(7),
        CREATE_DIALOG(25), ADDED_DIALOG(21), NAME_DIALOG(22), PHOTO_DIALOG(23), LEAVE_DIALOG(24);

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
        String ID = "dialog_notification_id";
        String STATE = "state";
        String BODY = "body";
        String CREATED_DATE = "created_date";
        String NOTIFICATION_TYPE = "notification_type";
    }
}