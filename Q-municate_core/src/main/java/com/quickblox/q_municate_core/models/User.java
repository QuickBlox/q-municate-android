package com.quickblox.q_municate_core.models;

import android.content.Context;

import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate_core.utils.OnlineStatusHelper;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    private Integer userId;
    private String fullName;
    private String email;
    private String login;
    private String phone;
    private String webSite;
    private UserCustomData userCustomData;
    private Date lastRequestAt;
    private String externalId;
    private String facebookId;
    private String twitterId;
    private Integer blobId;
    private String avatarUrl;
    private String status;
    private boolean online;
    private boolean selected;

    public User() {
    }

    public User(QBUser user) {
        this.userId = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.login = user.getLogin();
        this.phone = user.getPhone();
        this.webSite = user.getWebsite();
        this.lastRequestAt = user.getLastRequestAt();
        this.externalId = user.getExternalId();
        this.facebookId = user.getFacebookId();
        this.twitterId = user.getTwitterId();
        this.blobId = user.getFileId();
        // TODO Sergey Fedunets: temp
        this.avatarUrl = user.getWebsite();
        // end
    }

    public User(Integer userId, String fullName, String email, String phone, String avatarUrl) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getWebSite() {
        return webSite;
    }

    public void setWebSite(String webSite) {
        this.webSite = webSite;
    }

    public UserCustomData getUserCustomData() {
        return userCustomData;
    }

    public void setUserCustomData(UserCustomData userCustomData) {
        this.userCustomData = userCustomData;
    }

    public Date getLastRequestAt() {
        return lastRequestAt;
    }

    public void setLastRequestAt(Date lastRequestAt) {
        this.lastRequestAt = lastRequestAt;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    public String getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
    }

    public Integer getBlobId() {
        return blobId;
    }

    public void setBlobId(Integer blobId) {
        this.blobId = blobId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof User)) {
            return false;
        }

        User user = (User) object;

        return userId.equals(user.getUserId());
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }

    @Override
    public String toString() {
        return fullName;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getOnlineStatus(Context context) {
        return context.getResources().getString(OnlineStatusHelper.getOnlineStatus(online));
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
}