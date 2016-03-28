package com.quickblox.q_municate_core.models;

import com.quickblox.q_municate_core.utils.ConstsCore;

import java.io.Serializable;

public class UserCustomData implements Serializable {

    public static String TAG_AVATAR_URL = "avatar_url";
    public static String TAG_STATUS = "status";
    public static String TAG_IS_IMPORT = "is_import";

    private String avatar_url;
    private String status;
    private String is_import;

    public UserCustomData() {
        avatar_url = ConstsCore.EMPTY_STRING;
        status = ConstsCore.EMPTY_STRING;
    }

    public UserCustomData(String avatar_url, String status, String is_import) {
        this.avatar_url = avatar_url;
        this.status = status;
        this.is_import = is_import;
    }

    public String isIs_import() {
        return is_import;
    }

    public void setIs_import(String is_import) {
        this.is_import = is_import;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}