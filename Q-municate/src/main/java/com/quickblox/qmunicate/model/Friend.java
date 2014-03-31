package com.quickblox.qmunicate.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.caching.Models.CacheList;
import com.quickblox.qmunicate.utils.OnlineStatusHelper;
import com.quickblox.qmunicate.utils.UriCreator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@DatabaseTable
public class Friend implements Serializable {

    @DatabaseField
    private Integer id;

    @DatabaseField
    private String fullname;

    @DatabaseField
    private String email;

    @DatabaseField
    private String phone;

    @DatabaseField
    private Integer fileId;

    @DatabaseField
    private String avatarUid;

    @DatabaseField
    private String status;

    @DatabaseField
    private Date lastRequestAt;

    @DatabaseField
    private boolean online;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private CacheList cacheList;

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

    public Friend() {
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

    public String getAvatarUid() {
        return avatarUid;
    }

    public void setAvatarUid(String avatarUid) {
        this.avatarUid = avatarUid;
    }

    public CacheList getCacheList() {
        return cacheList;
    }

    public void setCacheList(CacheList cacheList) {
        this.cacheList = cacheList;
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

    public Date getLastRequestAt() {
        return lastRequestAt;
    }

    public void setLastRequestAt(Date lastRequestAt) {
        this.lastRequestAt = lastRequestAt;
    }

    public String getOnlineStatus() {
        return OnlineStatusHelper.getOnlineStatus(lastRequestAt);
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
        return OnlineStatusHelper.isOnline(lastRequestAt);
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
}
