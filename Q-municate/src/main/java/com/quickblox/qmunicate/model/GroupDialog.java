package com.quickblox.qmunicate.model;

import com.quickblox.module.chat.model.QBDialog;

import java.util.ArrayList;

public class GroupDialog extends Dialog {

    private String roomJid;
    private ArrayList<Friend> occupantList;
    private int unreadMessageCount;

    public GroupDialog(String id) {
        super(id);
    }

    public GroupDialog(QBDialog dialog) {
        super(dialog);
        roomJid = dialog.getRoomJid();
        occupantList = new ArrayList<Friend>();
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

    public ArrayList<Friend> getOccupantList() {
        return occupantList;
    }

    public void setOccupantList(ArrayList<Friend> occupantList) {
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
        for (Friend friend : occupantList) {
            if (friend.isOnline()) {
                onlineOccupantsCount++;
            }
        }
        return onlineOccupantsCount;
    }
}