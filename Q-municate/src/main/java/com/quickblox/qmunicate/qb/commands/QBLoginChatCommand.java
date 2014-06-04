package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.qb.helpers.QBAuthHelper;
import com.quickblox.qmunicate.qb.helpers.QBChatHelper;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class QBLoginChatCommand extends ServiceCommand {

    private QBAuthHelper authHelper;
    private QBChatHelper chatHelper;

    public QBLoginChatCommand(Context context, QBAuthHelper authHelper, QBChatHelper chatHelper, String successAction,
            String failAction) {
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
        return extras;
    }

    private void tryLogin() throws XMPPException, IOException, SmackException {
        while (!chatHelper.isLoggedIn()) {
            try {
                chatHelper.login(authHelper.getUser());
            } catch (SmackException.ResourceBindingNotOfferedException ignore) { /* NOP */ }
        }
    }
}