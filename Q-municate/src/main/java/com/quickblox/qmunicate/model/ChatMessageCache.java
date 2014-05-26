package com.quickblox.qmunicate.model;

public class ChatMessageCache {

    private String message;
    private Integer senderId;
    private Integer chatId;
    private String attachUrl;
    private String roomJid;

    public ChatMessageCache(String message, int senderId, int chatId, String attachUrl) {
        this.message = message;
        this.senderId = senderId;
        this.chatId = chatId;
        this.attachUrl = attachUrl;
    }

    public ChatMessageCache(String message, int senderId, String roomJid, String attachUrl) {
        this.message = message;
        this.senderId = senderId;
        this.roomJid = roomJid;
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

    public Integer getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public void setAttachUrl(String attachUrl) {
        this.attachUrl = attachUrl;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }
}
