package com.quickblox.q_municate_db.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = DialogOccupant.TABLE_NAME)
public class DialogOccupant implements Serializable {

    public static final String TABLE_NAME = "dialog_occupant";

    public static final String COLUMN_DIALOG_OCCUPANT_ID = "dialog_occupant_id";

    @DatabaseField(generatedId = true, unique = true, columnName = COLUMN_DIALOG_OCCUPANT_ID)
    private int dialogOccupantId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, uniqueCombo = true,
            columnName = Dialog.COLUMN_DIALOG_ID)
    private Dialog dialog;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false, uniqueCombo = true,
            columnName = User.COLUMN_USER_ID)
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
}