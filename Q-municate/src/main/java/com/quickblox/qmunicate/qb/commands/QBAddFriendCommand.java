package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

public class QBAddFriendCommand extends ServiceCommand {

    private static final String TAG = QBAddFriendCommand.class.getSimpleName();

    public QBAddFriendCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, Friend friend) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, friend);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        Friend friend = (Friend) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        QBCustomObject newObject = new QBCustomObject(Consts.FRIEND_CLASS_NAME);
        newObject.put(Consts.FRIEND_FIELD_FRIEND_ID, friend.getId());

        QBCustomObjects.createObject(newObject);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND, friend);

        return result;
    }
}
