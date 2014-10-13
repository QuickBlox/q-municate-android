package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.errors.QBChatErrorsConstants;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.Consts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.Date;

public class QBLoginChatCommand extends ServiceCommand {

    private static final String TAG = QBLoginChatCommand.class.getSimpleName();

    private QBChatRestHelper chatRestHelper;

    public QBLoginChatCommand(Context context, QBAuthHelper authHelper, QBChatRestHelper chatRestHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        // TODO IS remove when fix ResourceBindingNotOfferedException occurrence
        tryLogin();
        if (!chatRestHelper.isLoggedIn()) {
            throw new Exception(QBChatErrorsConstants.AUTHENTICATION_FAILED);
        }
        return extras;
    }

    private void tryLogin() throws XMPPException, IOException, SmackException, QBResponseException {
        long startTime = new Date().getTime();
        long currentTime = startTime;
        while (!chatRestHelper.isLoggedIn() && (currentTime - startTime) < Consts.LOGIN_TIMEOUT) {
            currentTime = new Date().getTime();
            try {
                chatRestHelper.login(AppSession.getSession().getUser());
            } catch (SmackException ignore) { /* NOP */ }
        }
    }
}