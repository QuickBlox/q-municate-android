package com.quickblox.qmunicate.model;

import org.jivesoftware.smack.XMPPException;

public abstract class Chat {

    protected String name;
    protected Integer avatarId;
    protected String lastMessage;

    public abstract String getId();

    public abstract ChatType getType();

    public abstract String getName();

    public abstract int getAvatarId();

    public abstract ChatMessage getLastMessage();

    public void setLastMessage(String lastMessage){
        this.lastMessage = lastMessage;
    }

    public abstract void sendMessage(String message) throws XMPPException;

    public enum ChatType {PRIVATE, GROUP}
}