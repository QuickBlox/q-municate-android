package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.mediacall.CallActivity;

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