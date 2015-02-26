package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = Message.TABLE_NAME)
public class Message implements Serializable {

    public static final String TABLE_NAME = "message";

    public static final String COLUMN_MESSAGE_ID = "message_id";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_CREATED_DATE = "created_date";

    @DatabaseField(id = true, unique = true, columnName = COLUMN_MESSAGE_ID)
    private String messageId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = DialogOccupant.COLUMN_DIALOG_OCCUPANT_ID)
    private DialogOccupant dialogOccupant;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = State.COLUMN_STATE_ID)
    private State state;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = true,
            columnName = Attachment.COLUMN_ATTACHMENT_ID)
    private Attachment attachment;

    @DatabaseField(columnName = COLUMN_BODY)
    private String body;

    @DatabaseField(columnName = COLUMN_CREATED_DATE)
    private long createdDate;

    public Message() {
    }

    public Message(String messageId, DialogOccupant dialogOccupant, State state, Attachment attachment,
            String body, long createdDate) {
        this.messageId = messageId;
        this.dialogOccupant = dialogOccupant;
        this.state = state;
        this.attachment = attachment;
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
}