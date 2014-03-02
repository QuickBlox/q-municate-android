package com.quickblox.qmunicate.model;

import org.jivesoftware.smack.XMPPException;

import java.util.Date;

public class GroupChat extends Chat {

    public GroupChat() {

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

    @Override
    public ChatMessage getLastMessage() {
        return new ChatMessage("bye", new Date(), false);
    }

    @Override
    public void sendMessage(String message) throws XMPPException {
        // TODO send message to group chat
    }
}
