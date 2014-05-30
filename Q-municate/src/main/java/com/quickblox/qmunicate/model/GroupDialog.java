package com.quickblox.qmunicate.model;

import java.io.Serializable;
import java.util.List;

public class GroupDialog extends Chat implements Serializable {
    private List<Friend> opponents;
    private List<DialogMessage> messages;

    public GroupDialog(String name, Integer avatarId) {
        this.name = name;
        this.avatarId = avatarId;
    }

    @Override
    public String getId() {
        return "1";
    }

    @Override
    public ChatType getType() {
        return ChatType.GROUP;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int getAvatarId() {
        return 0;
    }

    public void setAvatarId(int avatarId) {
        this.avatarId = avatarId;
    }

    public List<Friend> getOpponentsList() {
        return opponents;
    }

    public List<DialogMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<DialogMessage> messages) {
        this.messages = messages;
    }

    public void setOpponents(List<Friend> opponents) {
        this.opponents = opponents;
    }

    @Override
    public DialogMessage getLastMessage() {
        DialogMessage message = new DialogMessage();
        message.setBody(this.lastMessage);
        return message;
    }
}