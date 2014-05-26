package com.quickblox.qmunicate.model;

import android.text.TextUtils;

public class ChatMessageCache {

    private String message;
    private int senderId;
    private String chatId;
    private String attachUrl;
    private String opponentName;
    private String membersIds;
    private boolean isGroup;

    public ChatMessageCache(String message, int senderId, String chatId, String attachUrl,
            String opponentName, String membersIds) {
        this.message = message;
        this.senderId = senderId;
        this.chatId = chatId;
        this.attachUrl = attachUrl;
        this.opponentName = opponentName;
        setGroup(!TextUtils.isEmpty(membersIds));
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

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getAttachUrl() {
        return attachUrl;
    }

    public void setAttachUrl(String attachUrl) {
        this.attachUrl = attachUrl;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public void setOpponentName(String opponentName) {
        this.opponentName = opponentName;
    }

    public boolean isGroup() {
        return isGroup;
    }

    private void setGroup(boolean isGroup) {
        this.isGroup = isGroup;
    }

    public String getMembersIds() {
        return membersIds;
    }

    public void setMembersIds(String membersIds) {
        this.membersIds = membersIds;
    }
}
