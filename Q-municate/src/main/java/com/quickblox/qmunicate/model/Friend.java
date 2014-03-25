package com.quickblox.qmunicate.model;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.ui.utils.OnlineStatusHelper;
import com.quickblox.qmunicate.ui.utils.UriCreator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Friend implements Serializable {

    private Integer id;
    private String fullname;
    private String email;
    private String phone;
    private Integer fileId;
    private String avatarUid;
    private String status;
    private Date lastRequestAt;
    private boolean online;

    private boolean selected;

    public Friend(QBUser user) {
        this.id = user.getId();
        this.fullname = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.fileId = user.getFileId();
        this.lastRequestAt = user.getLastRequestAt();
        this.avatarUid = UriCreator.cutUid(user.getWebsite());
    }

    public static List<Friend> createFriends(List<QBUser> users) {
        List<Friend> friends = new ArrayList<Friend>();
        for (QBUser user : users) {
            friends.add(new Friend(user));
        }
        return friends;
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public boolean isOnline() {
        return OnlineStatusHelper.isOnline(lastRequestAt);
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getLastRequestAt() {
        return lastRequestAt;
    }

    public void setLastRequestAt(Date lastRequestAt) {
        this.lastRequestAt = lastRequestAt;
    }

    public String getOnlineStatus() {
        return OnlineStatusHelper.getOnlineStatus(lastRequestAt);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getAvatarUid() {
        return avatarUid;
    }

    public void setAvatarUid(String avatarUid) {
        this.avatarUid = avatarUid;
    }
}
