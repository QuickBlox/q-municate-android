package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class QBAddFriendsCommand extends ServiceCommand {
    private static final String TAG = QBAddFriendsCommand.class.getSimpleName();

    public QBAddFriendsCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, String[] users) {
        Intent intent = new Intent(QBServiceConsts.ADD_FRIENDS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_FRIEND, users);
        context.startService(intent);
    }

    @Override
    protected Bundle perform(Bundle extras) throws Exception {
        String[] users = (String[]) extras.getSerializable(QBServiceConsts.EXTRA_FRIEND);

        List<QBCustomObject> customObjects = convertArray(users);
        QBCustomObjects.createObjects(customObjects);

        Bundle result = new Bundle();
        result.putSerializable(QBServiceConsts.EXTRA_FRIEND, users);

        return result;
    }

    private List<QBCustomObject> convertArray(String[] friends) {
        ArrayList<QBCustomObject> customObjects = new ArrayList<QBCustomObject>();
        QBCustomObject newObject;
        for (int i = 0; i < friends.length; i++) {
            newObject = new QBCustomObject(Consts.FRIEND_CLASS_NAME);
            newObject.put(Consts.FRIEND_FIELD_FRIEND_ID, friends[i]);
            customObjects.add(newObject);
        }
        return customObjects;
    }
}