package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = State.TABLE_NAME)
public class State implements Serializable {

    public static final String TABLE_NAME = "state";

    public static final String COLUMN_STATE_ID = "state_id";
    public static final String COLUMN_STATE = "state";

    @DatabaseField(generatedId = true, columnName = COLUMN_STATE_ID)
    private int stateId;

    @DatabaseField(unique = true, dataType = DataType.ENUM_INTEGER, columnName = COLUMN_STATE)
    private Type type;

    public State() {
    }

    public State(Type type) {
        this.type = type;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        DELIVERED, READ, SYNC
    }
}