package com.quickblox.q_municate.model;

import java.io.Serializable;

public class Friend implements Serializable {

    private Integer userId;
    private String relationStatus;

    public Friend() {
    }

    public Friend(Integer userId, String relationStatus) {
        this.userId = userId;
        this.relationStatus = relationStatus;
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