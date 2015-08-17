package com.quickblox.q_municate_db.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import static com.quickblox.q_municate_db.models.Dialog.Column.ID;
import static com.quickblox.q_municate_db.models.Dialog.Column.PHOTO;
import static com.quickblox.q_municate_db.models.Dialog.Column.ROOM_JID;
import static com.quickblox.q_municate_db.models.Dialog.Column.TABLE_NAME;
import static com.quickblox.q_municate_db.models.Dialog.Column.TITLE;
import static com.quickblox.q_municate_db.models.Dialog.Column.TYPE;

@DatabaseTable(tableName = TABLE_NAME)
public class Dialog implements Serializable {

    @DatabaseField(
            id = true,
            unique = true,
            columnName = ID)
    private String dialogId;

    @DatabaseField(
            columnName = TYPE)
    private Type type;

    @DatabaseField(
            columnName = ROOM_JID)
    private String roomJid;

    @DatabaseField(
            columnName = TITLE)
    private String title;

    @DatabaseField(
            columnName = PHOTO)
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {

        PRIVATE(0),
        GROUP(1);

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

        String TABLE_NAME = "dialog";
        String ID = "dialog_id";
        String ROOM_JID = "room_jid";
        String TITLE = "title";
        String PHOTO = "photo";
        String TYPE = "type";
    }
}