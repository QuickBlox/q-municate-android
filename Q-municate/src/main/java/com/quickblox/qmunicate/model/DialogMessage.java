package com.quickblox.qmunicate.model;

public class DialogMessage {

    private String id;
    private String body;
    private long time;
    private int senderId;
    private boolean incoming;

    public DialogMessage(String id, String body, long time, int senderId, boolean incoming) {
        this.id = id;
        this.body = body;
        this.time = time;
        this.senderId = senderId;
        this.incoming = incoming;
    }

    public DialogMessage() {
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}