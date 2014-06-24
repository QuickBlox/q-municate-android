package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBFriendListHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import java.util.List;

public class QBLoadFriendListCommand extends ServiceCommand {

    private QBFriendListHelper friendListHelper;

    public QBLoadFriendListCommand(Context context, QBFriendListHelper friendListHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.friendListHelper = friendListHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOAD_FRIENDS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        List<Integer> userIdsList = friendListHelper.updateFriendList();
        Bundle bundle = new Bundle();
        bundle.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) userIdsList);
        return bundle;
    }
}