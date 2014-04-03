package com.quickblox.qmunicate.ui.main;

import android.content.Context;

import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.service.QBChatHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RoomOccupantsLoader extends AbsFriendListLoader {

    public static final int ID = 4;

    private QBChatHelper qbChatHelper;

    public RoomOccupantsLoader(Context context, QBChatHelper qbChatHelper) {
        super(context);
        this.qbChatHelper = qbChatHelper;
    }

    @Override
    protected List<Integer> getUserIds() throws Exception {
        Collection<Integer> roomUsers = qbChatHelper.getJoinedRoom().getRoomUsers();
        List<Integer> userIds = new ArrayList<Integer>();
        userIds.addAll(roomUsers);
        Integer ownUserId = App.getInstance().getUser().getId();
        userIds.remove(ownUserId);
        return userIds;
    }
}
