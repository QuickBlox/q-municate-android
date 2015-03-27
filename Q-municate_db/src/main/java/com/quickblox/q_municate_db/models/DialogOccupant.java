package com.quickblox.q_municate_db.models;

import android.provider.BaseColumns;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.DialogOccupant.Column.ID;
import static com.quickblox.q_municate_db.models.DialogOccupant.Column.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class DialogOccupant implements Serializable {

    @DatabaseField(generatedId = true, unique = true, columnName = ID)
    private int dialogOccupantId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, uniqueCombo = true,
            columnName = Dialog.Column.ID)
    private Dialog dialog;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, uniqueCombo = true,
            columnName = User.Column.ID)
    private User user;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Message> messageCollection;

    public DialogOccupant() {
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

    public int getDialogOccupantId() {
        return dialogOccupantId;
    }

    public void setDialogOccupantId(int dialogOccupantId) {
        this.dialogOccupantId = dialogOccupantId;
    }

    public interface Column {

        String TABLE_NAME = "dialog_occupant";
        String ID = BaseColumns._ID;
    }
}