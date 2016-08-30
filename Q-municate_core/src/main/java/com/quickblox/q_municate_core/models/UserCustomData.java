package com.quickblox.q_municate_core.models;

import com.quickblox.q_municate_core.utils.ConstsCore;

import java.io.Serializable;

public class UserCustomData implements Serializable {

    public static String TAG_AVATAR_URL = "avatar_url";
    public static String TAG_STATUS = "status";
    public static String TAG_IS_IMPORT = "is_import";

    private String avatarUrl;
    private String status;
    private String isImport;

    public UserCustomData() {
        avatarUrl = ConstsCore.EMPTY_STRING;
        status = ConstsCore.EMPTY_STRING;
    }

    public UserCustomData(String avatar_url, String status, String is_import) {
        this.avatarUrl = avatar_url;
        this.status = status;
        this.isImport = is_import;
    }

    public String isImport() {
        return isImport;
    }

    public void setIsImport(String isImport) {
        this.isImport = isImport;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatarUrl = avatar_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}