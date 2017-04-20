package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.DialogNotification.Column.BODY;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.CREATED_DATE;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.ID;
import static com.quickblox.q_municate_db.models.DialogNotification.Column.TYPE;
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
            canBeNull = false,
            columnDefinition = "INTEGER REFERENCES " + DialogOccupant.Column.TABLE_NAME + "(" + DialogOccupant.Column.ID + ") ON DELETE CASCADE",
            columnName = DialogOccupant.Column.ID)
    private DialogOccupant dialogOccupant;

    @DatabaseField(
            columnName = STATE)
    private State state;

    @DatabaseField(
            columnName = TYPE)
    private Type type;

    @DatabaseField(
            columnName = BODY)
    private String body;

    @DatabaseField(
            columnName = CREATED_DATE)
    private long createdDate;

    public DialogNotification() {
    }

    public DialogNotification(String dialogNotificationId, DialogOccupant dialogOccupant, State state,
            Type type, long createdDate) {
        this.dialogNotificationId = dialogNotificationId;
        this.dialogOccupant = dialogOccupant;
        this.state = state;
        this.type = type;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
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

    @Override
    public String toString() {
        return "DialogNotification [dialogNotificationId='" + dialogNotificationId
                + "', dialogOccupant='" + dialogOccupant
                + "', body='" + body
                + "', createdDate='" + createdDate
                + "', state='" + state
                + "', type='" + type + "']";
    }

    public enum Type {

        FRIENDS_REQUEST(4), FRIENDS_ACCEPT(5), FRIENDS_REJECT(6), FRIENDS_REMOVE(7),
        CREATE_DIALOG(25), ADDED_DIALOG(21), NAME_DIALOG(22), PHOTO_DIALOG(23), OCCUPANTS_DIALOG(24);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public static Type parseByCode(int code) {
            Type[] valuesArray = Type.values();
            Type result = null;
            for (Type value : valuesArray) {
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
        String TYPE = "type";
    }
}