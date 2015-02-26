package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Role.TABLE_NAME)
public class Role {

    public static final String TABLE_NAME = "role";

    public static final String COLUMN_ROLE_ID = "role_id";
    public static final String COLUMN_ROLE = "role";

    @DatabaseField(generatedId = true, columnName = COLUMN_ROLE_ID)
    private int roleId;

    @DatabaseField(unique = true, dataType = DataType.ENUM_INTEGER, columnName = COLUMN_ROLE)
    private Type type;

    public Role() {
    }

    public Role(Type type) {
        this.type = type;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        OWNER, SIMPLE_ROLE
    }
}