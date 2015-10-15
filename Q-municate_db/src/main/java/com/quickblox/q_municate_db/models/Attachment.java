package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.Attachment.Column.ADDITIONAL_INFO;
import static com.quickblox.q_municate_db.models.Attachment.Column.BLOB_ID;
import static com.quickblox.q_municate_db.models.Attachment.Column.ID;
import static com.quickblox.q_municate_db.models.Attachment.Column.NAME;
import static com.quickblox.q_municate_db.models.Attachment.Column.REMOTE_URL;
import static com.quickblox.q_municate_db.models.Attachment.Column.SIZE;
import static com.quickblox.q_municate_db.models.Attachment.Column.TABLE_NAME;
import static com.quickblox.q_municate_db.models.Attachment.Column.TYPE;

@DatabaseTable(tableName = TABLE_NAME)
public class Attachment implements Serializable {

    @DatabaseField(
            id = true,
            unique = true,
            columnName = ID)
    private String attachmentId;

    @DatabaseField(
            columnName = BLOB_ID)
    private int blobId;

    @DatabaseField(
            columnName = TYPE)
    private Type type;

    @DatabaseField(
            columnName = NAME)
    private String name;

    @DatabaseField(
            columnName = SIZE)
    private double size;

    @DatabaseField(
            columnName = REMOTE_URL)
    private String remoteUrl;

    @DatabaseField(
            columnName = ADDITIONAL_INFO)
    private String additionalInfo;

    public Attachment() {
    }

    public Attachment(String attachmentId, int blobId, Type type, String name, long size, String remoteUrl,
            String additionalInfo) {
        this.attachmentId = attachmentId;
        this.blobId = blobId;
        this.type = type;
        this.name = name;
        this.size = size;
        this.remoteUrl = remoteUrl;
        this.additionalInfo = additionalInfo;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public int getBlobId() {
        return blobId;
    }

    public void setBlobId(int blobId) {
        this.blobId = blobId;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
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

    @Override
    public String toString() {
        return "Attachment [attachmentId='" + attachmentId + "', remoteUrl='" + remoteUrl + "']";
    }

    public enum Type {

        AUDIO(0),
        VIDEO(1),
        PICTURE(2),
        DOC(3),
        OTHER(4);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public static Type parseByCode(int code) {
            Type[] valuesArray = Type.values();
            Type result = null;
            for (Type value : valuesArray) {
                if (value.getCode() == code) {
                    result = value;
                    break;
                }
            }
            return result;
        }

        public int getCode() {
            return code;
        }
    }

    public interface Column {

        String TABLE_NAME = "attachment";
        String ID = "attachment_id";
        String BLOB_ID = "blob_id";
        String NAME = "name";
        String SIZE = "size";
        String REMOTE_URL = "remote_url";
        String ADDITIONAL_INFO = "additional_info";
        String TYPE = "type";
    }
}