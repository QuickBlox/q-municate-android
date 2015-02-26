package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = SocialType.TABLE_NAME)
public class SocialType {

    public static final String TABLE_NAME = "social_type";

    public static final String COLUMN_TYPE_ID = "type_id";
    public static final String COLUMN_TYPE = "type";

    @DatabaseField(generatedId = true, columnName = COLUMN_TYPE_ID)
    private int typeId;

    @DatabaseField(unique = true, dataType = DataType.ENUM_INTEGER,
            columnName = COLUMN_TYPE)
    private Type type;

    public SocialType() {
    }

    public SocialType(Type type) {
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
        FACEBOOK, TWITTER
    }
}