package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public class QBRemoveFriendCommand extends ServiceCommand {

    private static final String TAG = QBRemoveFriendCommand.class.getSimpleName();

    public QBRemoveFriendCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, Friend friend) {
        Intent intent = new Intent(QBServiceConsts.REMOVE_FRIEND_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, friend);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        //TODO VF Implementation will be changed
        Friend friend = (Friend) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        QBCustomObjectRequestBuilder builder = new QBCustomObjectRequestBuilder();
        builder.eq(Consts.FRIEND_FIELD_USER_ID, App.getInstance().getUser().getId());
        builder.eq(Consts.FRIEND_FIELD_FRIEND_ID, friend.getId());

        List<QBCustomObject> objects = QBCustomObjects.getObjects(Consts.EXTRA_FRIEND, builder);

        QBCustomObjects.deleteObject(Consts.EXTRA_FRIEND, objects.get(0).getCustomObjectId());

        return null;
    }
}
