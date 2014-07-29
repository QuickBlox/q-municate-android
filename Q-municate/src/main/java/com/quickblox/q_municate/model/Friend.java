package com.quickblox.q_municate.model;

import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.OnlineStatusHelper;

import java.io.Serializable;

public class Friend implements Serializable {

    private Integer id;
    private String fullname;
    private String email;
    private String phone;
    private Integer fileId;
    private String avatarUrl;
    private String status = Consts.EMPTY_STRING;
    private boolean online;
    private Type type;

    private boolean selected;

    public Friend() {
    }

    public Friend(Integer id, String fullname, String email, String phone, Integer fileId, String avatarUrl,
            Type type) {
        this.id = id;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.fileId = fileId;
        this.avatarUrl = avatarUrl;
        this.type = type;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Friend)) {
            return false;
        }

        Friend friend = (Friend) object;

        return id.equals(friend.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return fullname;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOnlineStatus() {
        return OnlineStatusHelper.getOnlineStatus(online);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        none, to, from, both, remove;
    }
}