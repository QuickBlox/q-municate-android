package com.quickblox.qmunicate.model;

public class DialogMessageCache {

    private Integer senderId;
    private String message;
    private String attachUrl;
    private long time;
    private boolean isRead;

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean isSync) {
        this.isSync = isSync;
    }

    private boolean isSync;

    private String dialogId;

    public DialogMessageCache(String dialogId, Integer senderId, String message, String attachUrl, long time,
            boolean isRead, boolean isSync) {
        this.dialogId = dialogId;
        this.senderId = senderId;
        this.message = message;
        this.attachUrl = attachUrl;
        this.time = time;
        this.isRead = isRead;
        this.isSync = isSync;
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
}