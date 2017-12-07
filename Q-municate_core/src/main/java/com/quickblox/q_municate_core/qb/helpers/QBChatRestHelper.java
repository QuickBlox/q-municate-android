package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.util.Log;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.connections.tcp.QBTcpChatConnectionFabric;
import com.quickblox.chat.connections.tcp.QBTcpConfigurationBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.models.AppSession;
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

        QBTcpConfigurationBuilder configurationBuilder = new QBTcpConfigurationBuilder()
                .setSocketTimeout(0);

        QBChatService.setConnectionFabric(new QBTcpChatConnectionFabric(configurationBuilder));

        chatService = QBChatService.getInstance();

        chatService.removeConnectionListener(connectionListener);
        chatService.addConnectionListener(connectionListener);
    }

    public synchronized void login(QBUser user) throws XMPPException, IOException, SmackException {
        if (!chatService.isLoggedIn() && user != null) {
            if (QBProvider.FIREBASE_PHONE.equals(QBSessionManager.getInstance().getSessionParameters().getSocialProvider())
                    && !QBSessionManager.getInstance().isValidActiveSession()){
                CoreSharedHelper coreSharedHelper = new CoreSharedHelper(context);
                String currentFirebaseAccessToken = coreSharedHelper.getFirebaseToken();
                if (!QBSessionManager.getInstance().getSessionParameters().getAccessToken().equals(currentFirebaseAccessToken)) {
                    QBAuth.createSessionUsingFirebase(coreSharedHelper.getFirebaseProjectId(), currentFirebaseAccessToken).perform();
                    user.setPassword(QBSessionManager.getInstance().getToken());
                    AppSession.getSession().updateUser(user);
                }
            }
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

    private void tryReloginToChatUsingNewToken(){
        if (!chatService.isLoggedIn()
                && chatService.getUser() != null
                && QBSessionManager.getInstance().getSessionParameters() != null
                && QBSessionManager.getInstance().getSessionParameters().getSocialProvider() != null){

            chatService.login(AppSession.getSession().getUser(), (QBEntityCallback) null);
        }
    }

    private class ChatConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {
            Log.e(TAG, "connected");
        }

        @Override
        public void authenticated(XMPPConnection xmppConnection, boolean b) {
            Log.e(TAG, "authenticated");
        }

        @Override
        public void connectionClosed() {
            Log.e(TAG, "connectionClosed");
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.e(TAG, "connectionClosedOnError, error: " + e.getMessage());
            //TODO VT temp solution before test in SDK
            //need renew user password in QBChatService for user which was logged in
            //via social provider
            tryReloginToChatUsingNewToken();
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.e(TAG, "reconnectingIn(" + seconds + ")");
        }

        @Override
        public void reconnectionSuccessful() {
            Log.e(TAG, "reconnectionSuccessful()");
        }

        @Override
        public void reconnectionFailed(Exception error) {
            Log.e(TAG, "reconnectionFailed() " + error.getMessage());
            //TODO VT temp solution before test in SDK
            //need renew user password in QBChatService for user which was logged in
            //via social provider
            tryReloginToChatUsingNewToken();
        }
    }
}