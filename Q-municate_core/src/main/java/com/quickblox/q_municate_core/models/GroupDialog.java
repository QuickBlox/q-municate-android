package com.quickblox.q_municate_core.models;

import com.quickblox.chat.model.QBDialog;

import java.util.ArrayList;

public class GroupDialog extends Dialog {

    private String roomJid;
    private ArrayList<User> occupantList;
    private int unreadMessageCount;
    private String photoUrl;

    public GroupDialog(String id) {
        super(id);
    }

    public GroupDialog(QBDialog dialog) {
        super(dialog);
        roomJid = dialog.getRoomJid();
        occupantList = new ArrayList<User>();
        photoUrl = dialog.getPhoto();
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Override
    public DialogType getType() {
        return DialogType.GROUP;
    }

    public String getRoomJid() {
        return roomJid;
    }

    public void setRoomJid(String roomJid) {
        this.roomJid = roomJid;
    }

    public ArrayList<User> getOccupantList() {
        return occupantList;
    }

    public void setOccupantList(ArrayList<User> occupantList) {
        this.occupantList = occupantList;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public int getOccupantsCount() {
        return occupantList.size();
    }

    public int getOnlineOccupantsCount() {
        int onlineOccupantsCount = 0;
        for (User friend : occupantList) {
            if (friend.isOnline()) {
                onlineOccupantsCount++;
            }
        }
        return onlineOccupantsCount;
    }
}