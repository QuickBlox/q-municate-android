package com.quickblox.q_municate_user_service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by pelipets on 1/10/17.
 */

public class QMUserCustomData implements Serializable {

    public static String TAG_AVATAR_URL = "avatar_url";
    public static String TAG_STATUS = "status";
    public static String TAG_IS_IMPORT = "is_import";

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("status")
    private String status;

    @SerializedName("is_import")
    private String isImport;


    public QMUserCustomData() {
        avatarUrl = "";
        status = "";
    }

    public QMUserCustomData(String avatarUrl, String status, String isImport) {
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.isImport = isImport;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsImport() {
        return isImport;
    }

    public void setIsImport(String isImport) {
        this.isImport = isImport;
    }
}
