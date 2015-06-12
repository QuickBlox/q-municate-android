package com.quickblox.q_municate_core.qb.commands;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.exception.QBResponseException;
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

    public static void start(Context context, Class<? extends Activity> callClass) {
        Intent intent = new Intent(QBServiceConsts.INIT_VIDEO_CHAT_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CALL_ACTIVITY, callClass);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws QBResponseException {
        if (extras == null || extras.getSerializable(QBServiceConsts.EXTRA_CALL_ACTIVITY) == null) { // global init
            videoChatHelper.init(QBChatService.getInstance());
        } else {
            // init call activity
            videoChatHelper.initActivityClass((Class<? extends Activity>) extras.getSerializable(
                    QBServiceConsts.EXTRA_CALL_ACTIVITY));
        }

        return extras;
    }
}