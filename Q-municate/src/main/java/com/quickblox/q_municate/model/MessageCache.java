package com.quickblox.q_municate.model;

import java.io.Serializable;

public class MessageCache implements Serializable {

    private String id;
    private String dialogId;
    private String packetId;
    private Integer senderId;
    private String message;
    private String attachUrl;
    private long time;
    private boolean isRead;
    private boolean isDelivered;

    public MessageCache(String id, String dialogId, String packetId, Integer senderId, String message,
            String attachUrl, long time, boolean isRead, boolean isDelivered) {
        this.id = id;
        this.dialogId = dialogId;
        this.packetId = packetId;
        this.senderId = senderId;
        this.message = message;
        this.attachUrl = attachUrl;
        this.time = time;
        this.isRead = isRead;
        this.isDelivered = isDelivered;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean isDelivered) {
        this.isDelivered = isDelivered;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public void setAttachUrl(String attachUrl) {
        this.attachUrl = attachUrl;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }
}