package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtilsCore;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;

public class QBCreateGroupDialogCommand extends ServiceCommand {

    private QBMultiChatHelper multiChatHelper;

    public QBCreateGroupDialogCommand(Context context, QBMultiChatHelper multiChatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, String roomName, ArrayList<User> friendList) {
        Intent intent = new Intent(QBServiceConsts.CREATE_GROUP_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_ROOM_NAME, roomName);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendList);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws QBResponseException {
        ArrayList<User> friendList = (ArrayList<User>) extras.getSerializable(
                QBServiceConsts.EXTRA_FRIENDS);
        String roomName = (String) extras.getSerializable(QBServiceConsts.EXTRA_ROOM_NAME);

        QBDialog dialog = null;
        try {
            dialog = multiChatHelper.createGroupChat(roomName, ChatUtilsCore.getFriendIdsList(friendList));
        } catch (XMPPException | SmackException e) {
            throw  new QBResponseException(e.getLocalizedMessage());
        }
        extras.putSerializable(QBServiceConsts.EXTRA_DIALOG, dialog);
        return extras;
    }
}