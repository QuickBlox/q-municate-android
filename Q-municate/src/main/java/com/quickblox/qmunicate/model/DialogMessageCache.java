package com.quickblox.qmunicate.model;

public class DialogMessageCache {

    private String roomJidId;
    private Integer senderId;
    private String message;
    private String attachUrl;

    public DialogMessageCache(String roomJidId, int senderId, String message, String attachUrl) {
        this.roomJidId = roomJidId;
        this.senderId = senderId;
        this.message = message;
        this.attachUrl = attachUrl;
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

    public String getRoomJidId() {
        return roomJidId;
    }

    public void setRoomJidId(String roomJidId) {
        this.roomJidId = roomJidId;
    }
}