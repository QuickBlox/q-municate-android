package com.quickblox.qmunicate.model;

import java.util.Date;

public class ChatMessage {

    private String id;
    private String subject;
    private String body;
    private Date time;
    private String senderName;
    private int senderId;
    private boolean incoming;

    public ChatMessage(String id, String subject, String body, Date time, String senderName, int senderId,
            boolean incoming) {
        this.id = id;
        this.subject = subject;
        this.body = body;
        this.time = time;
        this.senderName = senderName;
        this.senderId = senderId;
        this.incoming = incoming;
    }

    public ChatMessage() {
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}