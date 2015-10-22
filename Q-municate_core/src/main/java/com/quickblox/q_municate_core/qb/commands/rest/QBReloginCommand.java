

package com.quickblox.q_municate_core.qb.commands.rest;

import android.content.Context;
import android.content.Intent;

import com.quickblox.q_municate_core.core.command.CompositeServiceCommand;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;

public class QBReloginCommand extends CompositeServiceCommand {

    public QBReloginCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.RE_LOGIN_IN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }
}