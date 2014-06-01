package com.quickblox.qmunicate.model;

import com.quickblox.module.chat.model.QBDialogType;

import java.io.Serializable;
import java.util.ArrayList;

public class Dialog implements Serializable {

    private String dialogId;
    private String lastMessage;
    private long lastMessageDateSent;
    private Integer lastMessageUserId;
    private String roomJidId;
    private Integer unreadMessageCount;
    private String name;
    private ArrayList<Integer> occupantsIds;
    private QBDialogType type;
    private Integer avatarId;

    public Dialog() {
    }

    public Integer getAvatarId() {
        return avatarId;
    }

    public void setAvatarId(Integer avatarId) {
        this.avatarId = avatarId;
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageDateSent() {
        return lastMessageDateSent;
    }

    public void setLastMessageDateSent(long lastMessageDateSent) {
        this.lastMessageDateSent = lastMessageDateSent;
    }

    public Integer getLastMessageUserId() {
        return lastMessageUserId;
    }

    public void setLastMessageUserId(Integer lastMessageUserId) {
        this.lastMessageUserId = lastMessageUserId;
    }

    public String getRoomJidId() {
        return roomJidId;
    }

    public void setRoomJidId(String roomJidId) {
        this.roomJidId = roomJidId;
    }

    public Integer getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(Integer unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Integer> getOccupantsIds() {
        return occupantsIds;
    }

    public void setOccupantsIds(ArrayList<Integer> occupantsIds) {
        this.occupantsIds = occupantsIds;
    }

    public QBDialogType getType() {
        return type;
    }

    public void setType(QBDialogType type) {
        this.type = type;
    }
}