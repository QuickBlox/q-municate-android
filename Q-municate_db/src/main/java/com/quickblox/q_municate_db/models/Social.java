package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.Social.Column.ID;
import static com.quickblox.q_municate_db.models.Social.Column.TABLE_NAME;
import static com.quickblox.q_municate_db.models.Social.Column.TYPE;

@DatabaseTable(tableName = TABLE_NAME)
public class Social implements Serializable {

    @DatabaseField(
            id = true,
            columnName = ID)
    private String socialId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            canBeNull = false,
            columnName = QMUserColumns.ID)
    private QMUser user;

    @DatabaseField(
            columnName = TYPE)
    private Type type;

    public Social() {
    }

    public Social(String socialId, QMUser user, Type type) {
        this.socialId = socialId;
        this.user = user;
        this.type = type;
    }

    public String getSocialId() {
        return socialId;
    }

    public void setSocialId(String socialId) {
        this.socialId = socialId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public QMUser getUser() {
        return user;
    }

    public void setUser(QMUser user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Social [socialId='" + socialId + "', user='" + user + "']";
    }

    public enum Type {
        FACEBOOK(0),
        TWITTER(1);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public static Type parseByCode(int code) {
            Type[] valuesArray = Type.values();
            Type result = null;
            for (Type value : valuesArray) {
                if (value.getCode() == code) {
                    result = value;
                    break;
                }
            }
            return result;
        }

        public int getCode() {
            return code;
        }
    }

    public interface Column {

        String TABLE_NAME = "social";
        String ID = "social_id";
        String TYPE = "type";
    }
}