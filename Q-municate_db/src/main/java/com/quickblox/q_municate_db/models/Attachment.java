package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = Attachment.TABLE_NAME)
public class Attachment implements Serializable {

    public static final String TABLE_NAME = "attachment";

    public static final String COLUMN_ATTACHMENT_ID = "attachment_id";
    public static final String COLUMN_BLOB_ID = "blob_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_SIZE = "size";
    public static final String COLUMN_REMOTE_URL = "remote_url";
    public static final String COLUMN_ADDITIONAL_INFO = "additional_info";

    @DatabaseField(generatedId = true, unique = true, columnName = COLUMN_ATTACHMENT_ID)
    private int attachmentId;

    @DatabaseField(unique = true, columnName = COLUMN_BLOB_ID)
    private int blobId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = AttachmentType.COLUMN_TYPE_ID)
    private AttachmentType attachmentType;

    @DatabaseField(columnName = COLUMN_NAME)
    private String name;

    @DatabaseField(columnName = COLUMN_SIZE)
    private long size;

    @DatabaseField(columnName = COLUMN_REMOTE_URL)
    private String remoteUrl;

    @DatabaseField(columnName = COLUMN_ADDITIONAL_INFO)
    private String additionalInfo;

    public Attachment() {
    }

    public Attachment(int blobId, AttachmentType attachmentType, String name, long size, String remoteUrl,
            String additionalInfo) {
        this.blobId = blobId;
        this.attachmentType = attachmentType;
        this.name = name;
        this.size = size;
        this.remoteUrl = remoteUrl;
        this.additionalInfo = additionalInfo;
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public int getBlobId() {
        return blobId;
    }

    public void setBlobId(int blobId) {
        this.blobId = blobId;
    }

    public AttachmentType getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(AttachmentType attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}