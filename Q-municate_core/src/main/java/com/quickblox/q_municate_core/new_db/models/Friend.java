package com.quickblox.q_municate_core.new_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable
public class Friend implements Serializable {

    @DatabaseField
    private Integer userId;

    @DatabaseField
    private boolean isPendingStatus;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private User user;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private FriendsRelationStatus friendsRelationStatus;

    public FriendsRelationStatus getFriendsRelationStatus() {
        return friendsRelationStatus;
    }

    public void setFriendsRelationStatus(FriendsRelationStatus friendsRelationStatus) {
        this.friendsRelationStatus = friendsRelationStatus;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isPendingStatus() {
        return isPendingStatus;
    }

    public void setPendingStatus(boolean isPendingStatus) {
        this.isPendingStatus = isPendingStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}