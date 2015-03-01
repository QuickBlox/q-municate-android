package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = DialogType.TABLE_NAME)
public class DialogType implements Serializable {

    public static final String TABLE_NAME = "dialog_type";

    public static final String COLUMN_TYPE_ID = "type_id";
    public static final String COLUMN_TYPE = "type";

    @DatabaseField(generatedId = true, columnName = COLUMN_TYPE_ID)
    private int typeId;

    @DatabaseField(unique = true, dataType = DataType.ENUM_INTEGER, columnName = COLUMN_TYPE)
    private Type type;

    public DialogType() {
    }

    public DialogType(Type type) {
        this.type = type;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        PRIVATE, GROUP
    }
}