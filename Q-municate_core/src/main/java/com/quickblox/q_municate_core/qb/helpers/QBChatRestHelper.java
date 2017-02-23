package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Lo;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;

public class QBChatRestHelper extends BaseHelper {

    private static final String TAG = QBChatRestHelper.class.getSimpleName();
    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;

    private QBChatService chatService;
    private ConnectionListener connectionListener = new ChatConnectionListener();

    public QBChatRestHelper(Context context) {
        super(context);
    }

    public synchronized void initChatService() throws XMPPException, SmackException {
        QBChatService.setDefaultPacketReplyTimeout(ConstsCore.DEFAULT_PACKET_REPLY_TIMEOUT);

        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
        configurationBuilder.setSocketTimeout(0);
        QBChatService.setConfigurationBuilder(configurationBuilder);


        chatService = QBChatService.getInstance();

        chatService.addConnectionListener(connectionListener);
    }

    public synchronized void login(QBUser user) throws XMPPException, IOException, SmackException {
        if (!chatService.isLoggedIn() && user != null) {
            chatService.login(user);
            chatService.enableCarbons();
        }
    }
    
    public synchronized void logout() throws QBResponseException, SmackException.NotConnectedException {
        if (chatService != null) {
            chatService.logout();
        }
    }

    public void destroy() {
        chatService.destroy();
    }

    public boolean isLoggedIn() {
        return chatService != null && chatService.isLoggedIn();
    }

    private class ChatConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {

        }

        @Override
        public void authenticated(XMPPConnection xmppConnection, boolean b) {
        }

        @Override
        public void connectionClosed() {
            Lo.g("connectionClosed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {

        }

        @Override
        public void reconnectingIn(int seconds) {
            Lo.g("reconnectingIn(" + seconds + ")");
        }

        @Override
        public void reconnectionSuccessful() {
        }

        @Override
        public void reconnectionFailed(Exception error) {
            Lo.g("reconnectionFailed() " + error.getMessage());
        }
    }
}