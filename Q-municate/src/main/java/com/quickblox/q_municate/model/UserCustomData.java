package com.quickblox.q_municate.model;

import com.quickblox.q_municate.utils.Consts;

import java.io.Serializable;

public class UserCustomData implements Serializable{

    private String avatar_url;
    private String status;
    private int is_import;

    public UserCustomData() {
        avatar_url = Consts.EMPTY_STRING;
        status = Consts.EMPTY_STRING;
    }

    public UserCustomData(String avatar_url, String status, int is_import) {
        this.avatar_url = avatar_url;
        this.status = status;
        this.is_import = is_import;
    }

    public int isIs_import() {
        return is_import;
    }

    public void setIs_import(int is_import) {
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