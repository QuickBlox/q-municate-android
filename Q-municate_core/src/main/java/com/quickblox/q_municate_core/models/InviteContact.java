package com.quickblox.q_municate_core.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class InviteContact implements Parcelable {

    public static final int VIA_PHONE_TYPE = 0;
    public static final int VIA_EMAIL_TYPE = 1;
    public static final int VIA_FACEBOOK_TYPE = 2;


    private String id;
    private String name;
    private String link;
    private int viaLabelType;
    private Uri uri;
    private boolean selected;

    private Integer qbId;
    private String qbName;
    private String qbAvatarUrl;

    public InviteContact(String id, String name, String link, int viaLabelType, Uri uri, boolean selected) {
        this.id = id;
        this.name = name;
        this.link = link;
        this.viaLabelType = viaLabelType;
        this.uri = uri;
        this.selected = selected;
    }

    protected InviteContact(Parcel in) {
        id = in.readString();
        name = in.readString();
        link = in.readString();
        viaLabelType = in.readInt();
        uri = in.readParcelable(Uri.class.getClassLoader());
        selected = in.readByte() != 0;
        qbName = in.readString();
        qbAvatarUrl = in.readString();
    }

    public static final Creator<InviteContact> CREATOR = new Creator<InviteContact>() {
        @Override
        public InviteContact createFromParcel(Parcel in) {
            return new InviteContact(in);
        }

        @Override
        public InviteContact[] newArray(int size) {
            return new InviteContact[size];
        }
    };

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

    public Integer getQbId() {
        return qbId;
    }

    public void setQbId(Integer qbId) {
        this.qbId = qbId;
    }

    public String getQbName() {
        return qbName;
    }

    public void setQbName(String qbName) {
        this.qbName = qbName;
    }

    public String getQbAvatarUrl() {
        return qbAvatarUrl;
    }

    public void setQbAvatarUrl(String qbAvatarUrl) {
        this.qbAvatarUrl = qbAvatarUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(link);
        dest.writeInt(viaLabelType);
        dest.writeParcelable(uri, flags);
        dest.writeByte((byte) (selected ? 1 : 0));
        dest.writeString(qbName);
        dest.writeString(qbAvatarUrl);
    }
}
