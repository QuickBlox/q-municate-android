package com.quickblox.q_municate.model;

import java.io.Serializable;

public class Friend implements Serializable {

    private Integer userId;
    private int relationStatusId;
    private String relationStatus;
    private int tempRelationStatusId;

    public Friend() {
    }

    public Friend(Integer userId, String relationStatus, int tempRelationStatusId) {
        this.userId = userId;
        this.relationStatus = relationStatus;
        this.tempRelationStatusId = tempRelationStatusId;
    }

    public int getTempRelationStatusId() {
        return tempRelationStatusId;
    }

    public void setTempRelationStatusId(int tempRelationStatusId) {
        this.tempRelationStatusId = tempRelationStatusId;
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