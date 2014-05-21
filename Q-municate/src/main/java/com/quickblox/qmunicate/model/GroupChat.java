package com.quickblox.qmunicate.model;

import org.jivesoftware.smack.XMPPException;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class GroupChat extends Chat implements Serializable {
    private List<Friend> opponents;
    private List<ChatMessage> messages;

    public GroupChat(String name, Integer avatarId) {
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

    public List<ChatMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ChatMessage> messages) {
        this.messages = messages;
    }

    public void setOpponents(List<Friend> opponents) {
        this.opponents = opponents;
    }

    @Override
    public ChatMessage getLastMessage() {
        ChatMessage message = new ChatMessage();
        message.setBody(this.lastMessage);
        return message;
    }

    @Override
    public void sendMessage(String message) throws XMPPException {
        // TODO send message to group chat
    }
}
