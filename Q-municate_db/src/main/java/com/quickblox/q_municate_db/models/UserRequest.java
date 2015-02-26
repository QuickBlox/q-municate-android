package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = UserRequest.TABLE_NAME)
public class UserRequest implements Serializable {

    public static final String TABLE_NAME = "user_request";

    public static final String COLUMN_REQUEST_ID = "request_id";
    public static final String COLUMN_TEXT_STATUS = "text_status";
    public static final String COLUMN_UPDATED_DATE = "updated_date";

    @DatabaseField(generatedId = true, unique = true, columnName = COLUMN_REQUEST_ID)
    private int requestId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, unique = true,
            canBeNull = false, columnName = User.COLUMN_USER_ID)
    private User user;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = Status.COLUMN_STATUS_ID)
    private Status status;

    @DatabaseField(columnName = COLUMN_TEXT_STATUS)
    private String textStatus;

    @DatabaseField(columnName = COLUMN_UPDATED_DATE)
    private long updatedDate;

    public UserRequest() {
    }

    public UserRequest(long updatedDate, String textStatus, Status status, User user) {
        this.updatedDate = updatedDate;
        this.textStatus = textStatus;
        this.status = status;
        this.user = user;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public long getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(long updatedDate) {
        this.updatedDate = updatedDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTextStatus() {
        return textStatus;
    }

    public void setTextStatus(String textStatus) {
        this.textStatus = textStatus;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isIncoming() {
        return user.getRole().getType() != Role.Type.OWNER;
    }
}