package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate.qb.helpers.QBMultiChatHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;

public class QBLogoutAndDestroyChatCommand extends ServiceCommand {

    private QBChatRestHelper chatRestHelper;
    private QBMultiChatHelper multiChatHelper;

    public QBLogoutAndDestroyChatCommand(Context context, QBChatRestHelper chatRestHelper, QBMultiChatHelper multiChatHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context, boolean destroyChat) {
        Intent intent = new Intent(QBServiceConsts.LOGOUT_AND_DESTROY_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.DESTROY_CHAT, destroyChat);
        context.startService(intent);
    }

    public static void start(Context context) {
       start(context, false);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        boolean destroy = true;
        if (extras != null) {
            destroy = extras.getBoolean(QBServiceConsts.DESTROY_CHAT, true);
        }
        if(chatRestHelper != null && chatRestHelper.isLoggedIn()) {
            multiChatHelper.leaveDialogs();
            chatRestHelper.logout();
            if (destroy) {
                chatRestHelper.destroy();
            }
        }
        return extras;
    }
}