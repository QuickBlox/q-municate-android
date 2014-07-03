package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.module.chat.QBChatService;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBVideoChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.mediacall.CallActivity;

public class QBInitVideoChatCommand extends ServiceCommand {

    private QBVideoChatHelper videoChatHelper;

    public QBInitVideoChatCommand(Context context, QBVideoChatHelper videoChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.videoChatHelper = videoChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.INIT_VIDEO_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        videoChatHelper.init(QBChatService.getInstance(), CallActivity.class);
        return extras;
    }
}