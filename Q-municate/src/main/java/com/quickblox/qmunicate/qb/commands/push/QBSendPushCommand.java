package com.quickblox.qmunicate.qb.commands.push;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.messages.QBMessages;
import com.quickblox.module.messages.model.QBEvent;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.core.gcm.NotificationHelper;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;

public class QBSendPushCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBSendPushCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, String message, ArrayList<Integer> friendIdsList) {
        Intent intent = new Intent(QBServiceConsts.SEND_PUSH_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendIdsList);
        intent.putExtra(Consts.PUSH_MESSAGE, message);
        context.startService(intent);
    }

    public static void start(Context context, String message, Integer friendId) {
        Intent intent = new Intent(QBServiceConsts.SEND_PUSH_ACTION, null, context, QBService.class);
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        friendIdsList.add(friendId);
        intent.putExtra(QBServiceConsts.EXTRA_FRIENDS, friendIdsList);
        intent.putExtra(Consts.PUSH_MESSAGE, message);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        ArrayList<Integer> usersIdsList = (ArrayList<Integer>) extras.getSerializable(
                QBServiceConsts.EXTRA_FRIENDS);
        String message = extras.getString(Consts.PUSH_MESSAGE);
        QBEvent pushEvent = NotificationHelper.createPushEvent(usersIdsList, message, null);
        QBMessages.createEvent(pushEvent);

        return null;
    }
}
