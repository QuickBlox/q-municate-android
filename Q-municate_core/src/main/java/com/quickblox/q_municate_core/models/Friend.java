package com.quickblox.q_municate_core.models;

import java.io.Serializable;

public class Friend implements Serializable {

    private Integer userId;
    private int relationStatusId;
    private String relationStatus;
    private boolean isAskStatus;

    public Friend() {
    }

    public Friend(Integer userId) {
        this.userId = userId;
    }

    public Friend(Integer userId, String relationStatus, boolean isAskStatus) {
        this.userId = userId;
        this.relationStatus = relationStatus;
        this.isAskStatus = isAskStatus;
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