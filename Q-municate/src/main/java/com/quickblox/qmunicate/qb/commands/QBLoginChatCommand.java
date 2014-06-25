package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.errors.QBChatErrors;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.Date;

public class QBLoginChatCommand extends ServiceCommand {

    private QBAuthHelper authHelper;
    private QBChatHelper chatHelper;

    public QBLoginChatCommand(Context context, QBAuthHelper authHelper, QBChatHelper chatHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.authHelper = authHelper;
        this.chatHelper = chatHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        // TODO IS remove when fix ResourceBindingNotOfferedException occurrence
        tryLogin();
        if (!chatHelper.isLoggedIn()) {
            throw new Exception(QBChatErrors.AUTHENTICATION_FAILED);
        }
        return extras;
    }

    private void tryLogin() throws XMPPException, IOException, SmackException, QBResponseException {
        long startTime = new Date().getTime();
        long currentTime = startTime;
        while (!chatHelper.isLoggedIn() && (currentTime - startTime) < Consts.LOGIN_TIMEOUT) {
            currentTime = new Date().getTime();
            chatHelper.login(AppSession.getSession().getUser());
        }
    }
}