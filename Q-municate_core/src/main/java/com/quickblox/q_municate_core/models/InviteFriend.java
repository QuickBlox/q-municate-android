package com.quickblox.q_municate_core.models;

import android.net.Uri;

import java.io.Serializable;

public class InviteFriend implements Serializable {

    public static final int VIA_FACEBOOK_TYPE = 0;
    public static final int VIA_CONTACTS_TYPE = 1;

    private String id;
    private String name;
    private String link;
    private int viaLabelType;
    private Uri uri;
    private boolean selected;

    public InviteFriend(String id, String name, String link, int viaLabelType, Uri uri, boolean selected) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.viaLabelType = viaLabelType;
        this.uri = uri;
        this.selected = selected;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getViaLabelType() {
        return viaLabelType;
    }

    public void setViaLabelType(int viaLabelType) {
        this.viaLabelType = viaLabelType;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
