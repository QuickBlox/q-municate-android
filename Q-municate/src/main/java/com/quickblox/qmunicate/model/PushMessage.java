package com.quickblox.qmunicate.model;

/**
 * Created by vadim on 17.03.14.
 */
public class PushMessage {

    private String message;
    private int userId;
    private String type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
