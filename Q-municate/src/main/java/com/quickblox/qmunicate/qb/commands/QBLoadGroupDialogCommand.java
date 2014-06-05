package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.GroupDialog;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QBLoadGroupDialogCommand extends ServiceCommand {

    private QBChatHelper chatHelper;

    public QBLoadGroupDialogCommand(Context context, QBChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatHelper = chatHelper;
    }

    public static void start(Context context, String roomJid) {
        Intent intent = new Intent(QBServiceConsts.LOAD_GROUP_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_ID, roomJid);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String roomJid = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_JID_ID);

        QBDialog dialog = DatabaseManager.getDialogByRoomJidId(context, roomJid);
        GroupDialog groupDialog = new GroupDialog(dialog);

        List<Integer> participantIds = dialog.getOccupants();
        List<Integer> onlineParticipantIds = chatHelper.getRoomOnlineParticipants(roomJid);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(Consts.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(Consts.FL_FRIENDS_PER_PAGE);

        Bundle requestParams = new Bundle();
        List<QBUser> users = QBUsers.getUsersByIDs(participantIds, requestBuilder, requestParams);
        Map<Integer, Friend> friendMap = Friend.createFriendMap(users);
        for (Integer onlineParticipantId : onlineParticipantIds) {
            friendMap.get(onlineParticipantId).setOnline(true);
        }

        ArrayList<Friend> friendList = new ArrayList<Friend>(friendMap.values());
        groupDialog.setOccupantList(friendList);

        Bundle params = new Bundle();
        params.putSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG, groupDialog);
        return params;
    }
}