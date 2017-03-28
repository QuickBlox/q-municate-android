package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.Message.Column.BODY;
import static com.quickblox.q_municate_db.models.Message.Column.CREATED_DATE;
import static com.quickblox.q_municate_db.models.Message.Column.ID;
import static com.quickblox.q_municate_db.models.Message.Column.STATE;
import static com.quickblox.q_municate_db.models.Message.Column.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class Message implements Serializable {

    @DatabaseField(
            id = true,
            unique = true,
            columnName = ID)
    private String messageId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            canBeNull = false,
            columnDefinition = "INTEGER REFERENCES " + DialogOccupant.Column.TABLE_NAME + "(" + DialogOccupant.Column.ID + ") ON DELETE CASCADE",
            columnName = DialogOccupant.Column.ID)
    private DialogOccupant dialogOccupant;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            canBeNull = true,
            columnName = Attachment.Column.ID)
    private Attachment attachment;

    @DatabaseField(
            columnName = STATE)
    private State state;

    @DatabaseField(
            columnName = BODY)
    private String body;

    @DatabaseField(
            columnName = CREATED_DATE)
    private long createdDate;

    public Message() {
    }

    public Message(String messageId, DialogOccupant dialogOccupant, Attachment attachment, State state,
            String body, long createdDate) {
        this.messageId = messageId;
        this.dialogOccupant = dialogOccupant;
        this.attachment = attachment;
        this.state = state;
        this.body = body;
        this.createdDate = createdDate;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public DialogOccupant getDialogOccupant() {
        return dialogOccupant;
    }

    public void setDialogOccupant(DialogOccupant dialogOccupant) {
        this.dialogOccupant = dialogOccupant;
    }

    public boolean isIncoming(int currentUserId) {
        return dialogOccupant != null &&  dialogOccupant.getUser() != null && currentUserId != dialogOccupant.getUser().getId();
    }

    @Override
    public String toString() {
        return "Message [messageId='" + messageId
                + "', dialogOccupant='" + dialogOccupant
                + "', body='" + body
                + "', createdDate='" + createdDate
                + "', state='" + state + "']";
    }

    public interface Column {

        String TABLE_NAME = "message";
        String ID = "message_id";
        String BODY = "body";
        String CREATED_DATE = "created_date";
        String STATE = "state";
    }
}