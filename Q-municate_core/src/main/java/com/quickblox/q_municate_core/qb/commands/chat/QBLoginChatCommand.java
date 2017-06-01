package com.quickblox.q_municate_core.qb.commands.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

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

        Log.i(TAG, "login with user login:" + currentUser.getLogin()
                + ", user id:" + currentUser.getId()
                + ", pswd=" + currentUser.getPassword() + ", fb id:" + currentUser.getFacebookId()
                + ", tw dg id:" + currentUser.getTwitterDigitsId());

        Log.i(TAG, "session token:" + QBSessionManager.getInstance().getToken()
                + "\n, token exp date: " + QBSessionManager.getInstance().getTokenExpirationDate()
                + "\n, is valid token:" + QBSessionManager.getInstance().isValidActiveSession());


        // We don't make login if QB session was deleted by one of expiration cases :
        // for ex when social provider token is no more valid
        if (QBSessionManager.getInstance().getSessionParameters() == null) {
            throw new QBResponseException("invalid session");
        }

        login(currentUser);

        return extras;
    }

    private void login(QBUser currentUser) throws XMPPException, IOException, SmackException {
        chatRestHelper.login(currentUser);
    }

}