package com.quickblox.q_municate_core.qb.commands;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.QBChatService;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.qb.helpers.QBVideoChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

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
        videoChatHelper.init(QBChatService.getInstance());
        return extras;
    }
}