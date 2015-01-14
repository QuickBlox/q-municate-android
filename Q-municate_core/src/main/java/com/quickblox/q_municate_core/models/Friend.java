package com.quickblox.q_municate_core.models;

import java.io.Serializable;

public class Friend implements Serializable {

    private Integer userId;
    private int relationStatusId;
    private String relationStatus;
    private boolean isPendingStatus;
    private boolean isNewFriendStatus;

    public Friend() {
    }

    public Friend(Integer userId) {
        this.userId = userId;
    }

    public Friend(Integer userId, String relationStatus, boolean isPendingStatus) {
        this.userId = userId;
        this.relationStatus = relationStatus;
        this.isPendingStatus = isPendingStatus;
    }

    public boolean isNewFriendStatus() {
        return isNewFriendStatus;
    }

    public void setNewFriendStatus(boolean isNewFriend) {
        this.isNewFriendStatus = isNewFriend;
    }

    public boolean isPendingStatus() {
        return isPendingStatus;
    }

    public void setPendingStatus(boolean isPendingStatus) {
        this.isPendingStatus = isPendingStatus;
    }

    public int getRelationStatusId() {
        return relationStatusId;
    }

    public void setRelationStatusId(int relationStatusId) {
        this.relationStatusId = relationStatusId;
    }

    public String getRelationStatus() {
        return relationStatus;
    }

    public void setRelationStatus(String relationStatus) {
        this.relationStatus = relationStatus;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}