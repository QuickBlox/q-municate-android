package com.quickblox.q_municate_core.models;

import com.quickblox.chat.model.QBDialog;

import java.io.Serializable;

public abstract class Dialog implements Serializable {

    protected String id;
    protected String name;
    protected String avatarUrl;
    protected String lastMessage;

    protected Dialog(QBDialog dialog) {
        id = dialog.getDialogId();
        name = dialog.getName();
        lastMessage = dialog.getLastMessage();
    }

    public Dialog(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public abstract DialogType getType();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public enum DialogType {PRIVATE, GROUP}
}