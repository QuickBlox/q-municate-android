package com.quickblox.q_municate_db.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.DialogOccupant.Column.ID;
import static com.quickblox.q_municate_db.models.DialogOccupant.Column.TABLE_NAME;
import static com.quickblox.q_municate_db.models.DialogOccupant.Column.STATUS;

@DatabaseTable(tableName = TABLE_NAME)
public class DialogOccupant implements Serializable {

    @DatabaseField(
            generatedId = true,
            unique = true,
            columnName = ID)
    private long dialogOccupantId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            canBeNull = false,
            uniqueCombo = true,
            columnDefinition = "INTEGER REFERENCES " + Dialog.Column.TABLE_NAME + "(" + Dialog.Column.ID + ") ON DELETE CASCADE",
            columnName = Dialog.Column.ID)
    private Dialog dialog;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            canBeNull = false,
            uniqueCombo = true,
            columnName = User.Column.ID)
    private User user;

    @ForeignCollectionField(
            eager = true)
    private ForeignCollection<Message> messageCollection;

    @DatabaseField(
            canBeNull = false,
            columnName = STATUS)
    private Status status;

    public DialogOccupant() {
        status = Status.ACTUAL;
    }

    public DialogOccupant(Dialog dialog, User user, ForeignCollection<Message> messageCollection) {
        this.dialog = dialog;
        this.user = user;
        this.messageCollection = messageCollection;
    }

    public ForeignCollection<Message> getMessages() {
        return messageCollection;
    }

    public void setMessages(ForeignCollection<Message> message) {
        this.messageCollection = message;
    }

    public Dialog getDialog() {
        return dialog;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ForeignCollection<Message> getMessageCollection() {
        return messageCollection;
    }

    public void setMessageCollection(ForeignCollection<Message> messageCollection) {
        this.messageCollection = messageCollection;
    }

    public long getDialogOccupantId() {
        return dialogOccupantId;
    }

    public void setDialogOccupantId(long dialogOccupantId) {
        this.dialogOccupantId = dialogOccupantId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof DialogOccupant)) {
            return false;
        }

        DialogOccupant dialogOccupant = (DialogOccupant) object;

        return dialogOccupantId == dialogOccupant.getDialogOccupantId();
    }

    @Override
    public String toString() {
        return "DialogOccupant [id='" + dialogOccupantId + "', dialog='" + dialog + "', user='" + user + "']";
    }

    public enum Status {

        ACTUAL(0),
        DELETED(1);

        private int code;

        Status(int code) {
            this.code = code;
        }

        public static Status parseByCode(int code) {
            Status[] valuesArray = Status.values();
            Status result = null;
            for (Status value : valuesArray) {
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

        String TABLE_NAME = "dialog_occupant";
        String ID = "dialog_occupant_id";
        String STATUS = "dialog_occupant_status";
    }
}