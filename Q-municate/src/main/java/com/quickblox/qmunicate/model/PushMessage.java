package com.quickblox.qmunicate.model;

/**
 * Created by vadim on 17.03.14.
 */
public class PushMessage {

    private String message;
    private int user_id;
    private String type;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getUserId() {
        return user_id;
    }

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
