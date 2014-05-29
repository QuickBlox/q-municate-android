package com.quickblox.qmunicate.model;

import java.io.Serializable;

public class ChatCache implements Serializable {

    private String dialogId;
    private String roomJidId;
    private String name;
    private int countUnreadMessages;
    private String lastMessage;
    private String[] occupantsIds;
    private int type;

    public ChatCache(String dialogId, String roomJidId, String name, int countUnreadMessages, String lastMessage,
            String[] occupantsIds, int type) {
        this.dialogId = dialogId;
        this.roomJidId = roomJidId;
        this.name = name;
        this.countUnreadMessages = countUnreadMessages;
        this.lastMessage = lastMessage;
        this.occupantsIds = occupantsIds;
        this.type = type;
    }

    public String getRoomJidId() {
        return roomJidId;
    }

    public void setRoomJidId(String roomJidId) {
        this.roomJidId = roomJidId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCountUnreadMessages() {
        return countUnreadMessages;
    }

    public void setCountUnreadMessages(int countUnreadMessages) {
        this.countUnreadMessages = countUnreadMessages;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String[] getOccupantsIds() {
        return occupantsIds;
    }

    public void setOccupantsIds(String[] occupantsIds) {
        this.occupantsIds = occupantsIds;
    }
}