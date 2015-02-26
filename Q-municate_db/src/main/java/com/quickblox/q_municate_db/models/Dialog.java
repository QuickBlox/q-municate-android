package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

@DatabaseTable(tableName = Dialog.TABLE_NAME)
public class Dialog implements Serializable {

    public static final String TABLE_NAME = "dialog";

    public static final String COLUMN_DIALOG_ID = "dialog_id";
    public static final String COLUMN_ROOM_JID = "room_jid";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_PHOTO = "photo";

    @DatabaseField(id = true, unique = true, columnName = COLUMN_DIALOG_ID)
    private String dialogId;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, canBeNull = false,
            columnName = DialogType.COLUMN_TYPE_ID)
    private DialogType dialogType;

    @DatabaseField(columnName = COLUMN_ROOM_JID)
    private String roomJid;

    @DatabaseField(columnName = COLUMN_TITLE)
    private String title;

    @DatabaseField(columnName = COLUMN_PHOTO)
    private String photo;

    public Dialog() {
    }

    public String getDialogId() {
        return dialogId;
    }

    public void setDialogId(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public DialogType getDialogType() {
        return dialogType;
    }

    public void setDialogType(DialogType dialogType) {
        this.dialogType = dialogType;
    }
}