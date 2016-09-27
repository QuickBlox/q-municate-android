package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBGroupChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBPrivateChatHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

public class QBInitChatsCommand extends ServiceCommand {

    private QBPrivateChatHelper privateChatHelper;
    private QBGroupChatHelper multiChatHelper;

    public QBInitChatsCommand(Context context, QBPrivateChatHelper privateChatHelper,
            QBGroupChatHelper multiChatHelper, String successAction, String failAction) {
        super(context, successAction, failAction);
        this.privateChatHelper = privateChatHelper;
        this.multiChatHelper = multiChatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.INIT_CHATS_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser user;

        if (extras == null) {
            user = AppSession.getSession().getUser();
        } else {
            user = (QBUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        }

        privateChatHelper.init(user);
        multiChatHelper.init(user);

        return extras;
    }
}