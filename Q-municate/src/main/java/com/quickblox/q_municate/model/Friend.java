package com.quickblox.q_municate.model;

import java.io.Serializable;

public class Friend implements Serializable {

    private Integer userId;
    private int relationStatusId;
    private String relationStatus;
    private boolean isAskStatus;
    private boolean isRequestedFriend;

    public Friend() {
    }

    public Friend(Integer userId, String relationStatus, boolean isAskStatus, boolean isRequestedFriend) {
        this.userId = userId;
        this.relationStatus = relationStatus;
        this.isAskStatus = isAskStatus;
        this.isRequestedFriend = isRequestedFriend;
    }

    public boolean isRequestedFriend() {
        return isRequestedFriend;
    }

    public void setRequestedFriend(boolean isRequestedFriend) {
        this.isRequestedFriend = isRequestedFriend;
    }

    public boolean isAskStatus() {
        return isAskStatus;
    }

    public void setAskStatus(boolean isAskStatus) {
        this.isAskStatus = isAskStatus;
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