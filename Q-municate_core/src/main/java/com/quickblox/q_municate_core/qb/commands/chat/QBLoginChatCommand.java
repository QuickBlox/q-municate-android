package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.chat.errors.QBChatErrorsConstants;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConnectivityUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;

import java.util.Date;

public class QBLoginChatCommand extends ServiceCommand {

    private static final String TAG = QBLoginChatCommand.class.getSimpleName();

    private QBChatRestHelper chatRestHelper;

    public QBLoginChatCommand(Context context, QBChatRestHelper chatRestHelper, String successAction,
            String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        final QBUser currentUser = AppSession.getSession().getUser();

        tryLogin(currentUser);

        if (!chatRestHelper.isLoggedIn()) {
            throw new Exception(QBChatErrorsConstants.AUTHENTICATION_FAILED);
        }

        return extras;
    }

    private void tryLogin(QBUser currentUser) throws Exception {
        long startTime = new Date().getTime();
        long currentTime = startTime;

        while (!chatRestHelper.isLoggedIn() && (currentTime - startTime) < ConstsCore.LOGIN_TIMEOUT) {
            currentTime = new Date().getTime();
            try {
                if (ConnectivityUtils.isNetworkAvailable(context)) {
                    chatRestHelper.login(currentUser);
                }
            } catch (SmackException ignore) {
                ignore.printStackTrace();
            }
        }
    }
}