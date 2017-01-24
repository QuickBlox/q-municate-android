package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.q_municate_user_service.model.QMUserColumns;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.Friend.Column.ID;
import static com.quickblox.q_municate_db.models.Friend.Column.TABLE_NAME;

@DatabaseTable(tableName = TABLE_NAME)
public class Friend implements Serializable {

    @DatabaseField(
            generatedId = true,
            unique = true,
            columnName = ID)
    private int friendId;

    @DatabaseField(
            foreign = true,
            foreignAutoRefresh = true,
            unique = true,
            canBeNull = false,
            columnName = QMUserColumns.ID)
    private QMUser user;

    public Friend() {
    }

    public Friend(QMUser user) {
        this.user = user;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public QMUser getUser() {
        return user;
    }

    public void setUser(QMUser user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Friend)) {
            return false;
        }

        Friend friend = (Friend) object;

        return user.getId() == friend.getUser().getId();
    }

    @Override
    public String toString() {
        return "Friend [friendId='" + friendId + "', user='" + user + "']";
    }

    public interface Column {

        String TABLE_NAME = "friend";
        String ID = "friend_id";
    }
}