package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatDialogUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;

public class QBJoinGroupDialogCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBJoinGroupDialogCommand(Context context, QBMultiChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, String roomJid) {
        ArrayList<String> roomJidList = new ArrayList<String>();
        roomJidList.add(roomJid);
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_JID_LIST, roomJidList);
        context.startService(intent);
    }

    public static void start(Context context, ArrayList<ParcelableQBDialog> dialogList) {
        Intent intent = new Intent(QBServiceConsts.JOIN_GROUP_CHAT_ACTION, null, context, QBService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(QBServiceConsts.EXTRA_ROOM_JID_LIST, dialogList);
        intent.putExtras(bundle);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws QBResponseException {
        ArrayList<ParcelableQBDialog> dialogList = null;
        List<QBDialog> dialogs = null;
        if (extras != null && extras.containsKey(QBServiceConsts.EXTRA_ROOM_JID_LIST)) {
            dialogList = extras.getParcelableArrayList(QBServiceConsts.EXTRA_ROOM_JID_LIST);
            dialogs = ChatDialogUtils.parcelableDialogsToDialogs(dialogList);
        }

        if(dialogs == null) {
            dialogs = ChatDatabaseManager.getDialogs(context);
        }

        if (dialogs != null && !dialogs.isEmpty()) {
            multiChatHelper.tryJoinRoomChats(dialogs);
            // save flag for join to dialogs
            PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_JOINED_TO_ALL_DIALOGS, true);
        }

        return extras;
    }
}