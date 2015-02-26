package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = Friend.TABLE_NAME)
public class Friend implements Serializable {

    public static final String TABLE_NAME = "friend";

    public static final String COLUMN_FRIEND_ID = "friend_id";

    @DatabaseField(generatedId = true, unique = true, columnName = COLUMN_FRIEND_ID)
    private int friendId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, unique = true, canBeNull = false,
            columnName = User.COLUMN_USER_ID)
    private User user;

    public Friend() {
    }

    public Friend(User user) {
        this.user = user;
    }

    public int getFriendId() {
        return friendId;
    }

    public void setFriendId(int friendId) {
        this.friendId = friendId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}