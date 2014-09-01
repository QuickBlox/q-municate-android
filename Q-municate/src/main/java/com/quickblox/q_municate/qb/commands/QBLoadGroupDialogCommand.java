package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.model.GroupDialog;
import com.quickblox.q_municate.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.FriendUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QBLoadGroupDialogCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBLoadGroupDialogCommand(Context context, QBMultiChatHelper chatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = chatHelper;
    }

    public static void start(Context context, String dialogId, String roomJid) {
        Intent intent = new Intent(QBServiceConsts.LOAD_GROUP_DIALOG_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID, roomJid);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        String roomJid = extras.getString(QBServiceConsts.EXTRA_ROOM_JID);

        QBDialog dialog = DatabaseManager.getDialogByDialogId(context, dialogId);
        GroupDialog groupDialog = new GroupDialog(dialog);

        List<Integer> participantIdsList = dialog.getOccupants();
        List<Integer> onlineParticipantIdsList = multiChatHelper.getRoomOnlineParticipantList(roomJid);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(Consts.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(Consts.FL_FRIENDS_PER_PAGE);

        Bundle requestParams = new Bundle();
        List<QBUser> userList = QBUsers.getUsersByIDs(participantIdsList, requestBuilder, requestParams);
        Map<Integer, Friend> friendMap = FriendUtils.createFriendMap(userList);
        for (Integer onlineParticipantId : onlineParticipantIdsList) {
            friendMap.get(onlineParticipantId).setOnline(true);
        }

        ArrayList<Friend> friendList = new ArrayList<Friend>(friendMap.values());
        groupDialog.setOccupantList(friendList);

        Bundle params = new Bundle();
        params.putSerializable(QBServiceConsts.EXTRA_GROUP_DIALOG, groupDialog);
        return params;
    }
}