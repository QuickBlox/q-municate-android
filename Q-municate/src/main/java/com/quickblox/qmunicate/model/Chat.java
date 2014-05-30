package com.quickblox.qmunicate.model;

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

    public enum ChatType {PRIVATE, GROUP}
}