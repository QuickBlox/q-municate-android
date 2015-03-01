package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = Status.TABLE_NAME)
public class Status implements Serializable {

    public static final String TABLE_NAME = "status";

    public static final String COLUMN_STATUS_ID = "status_id";
    public static final String COLUMN_STATUS = "status";

    @DatabaseField(generatedId = true, columnName = COLUMN_STATUS_ID)
    private int statusId;

    @DatabaseField(unique = true, dataType = DataType.ENUM_INTEGER, columnName = COLUMN_STATUS)
    private Type type;

    public Status() {
    }

    public Status(Type type) {
        this.type = type;
    }

    public int getStatusId() {
        return statusId;
    }

    public void setStatusId(int statusId) {
        this.statusId = statusId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        INCOMING, OUTGOING
    }
}