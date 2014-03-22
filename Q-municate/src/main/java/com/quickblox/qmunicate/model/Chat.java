package com.quickblox.qmunicate.model;

import org.jivesoftware.smack.XMPPException;

public abstract class Chat {

    protected String name;
    protected Integer avatarId;

    public abstract String getId();

    public abstract ChatType getType();

    public abstract String getName();

    public abstract int getAvatarId();

    public abstract ChatMessage getLastMessage();

    public abstract void sendMessage(String message) throws XMPPException;

    public enum ChatType {PRIVATE, GROUP}
}