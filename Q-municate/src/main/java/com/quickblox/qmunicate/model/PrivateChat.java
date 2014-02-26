package com.quickblox.qmunicate.model;

import org.jivesoftware.smack.XMPPException;

import java.util.Date;

public class PrivateChat extends Chat {

    private Friend friend;

    public PrivateChat(Friend friend) {
        this.friend = friend;
        // chat = QBChatService.getInstance().createChat();
    }

    @Override
    public String getId() {
        return String.valueOf(friend.getId());
    }

    @Override
    public ChatType getType() {
        return ChatType.PRIVATE;
    }

    @Override
    public String getName() {
        return friend.getFullname();
    }

    @Override
    public int getAvatarId() {
        return friend.getFileId();
    }

    @Override
    public ChatMessage getLastMessage() {
        return new ChatMessage("hi", new Date(), false);
    }

    @Override
    public void sendMessage(String message) throws XMPPException {
        // TODO send message to private chat
    }
}
