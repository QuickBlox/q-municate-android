package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.model.QBChatHistoryMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.Lo;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.List;

public class QBChatRestHelper extends BaseHelper {

    private static final int AUTO_PRESENCE_INTERVAL_IN_SECONDS = 30;
    private static final String TAG = QBChatRestHelper.class.getSimpleName();
    private QBChatService chatService;

    public QBChatRestHelper(Context context) {
        super(context);
    }

    private ConnectionListener connectionListener = new ChatConnectionListener();

    public synchronized void initChatService() throws XMPPException, SmackException {
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
            QBChatService.setDefaultPacketReplyTimeout(ConstsCore.DEFAULT_PACKET_REPLY_TIMEOUT);
            chatService = QBChatService.getInstance();
            chatService.addConnectionListener(connectionListener);
        }
    }

    public synchronized void login(
            QBUser user) throws XMPPException, IOException, SmackException {
        if (!chatService.isLoggedIn() && user != null) {
            chatService.login(user);
            chatService.enableCarbons();
            chatService.startAutoSendPresence(AUTO_PRESENCE_INTERVAL_IN_SECONDS);
        }
    }

    public synchronized void logout() throws QBResponseException, SmackException.NotConnectedException {
        if (chatService != null) {
            chatService.stopAutoSendPresence();
            chatService.logout();
        }
    }

    public void destroy() {
        chatService.destroy();
    }

    public boolean isLoggedIn() {
        return chatService != null && chatService.isLoggedIn();
    }

    public List<QBDialog> getDialogs() throws QBResponseException, XMPPException, SmackException {
        Bundle bundle = new Bundle();
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        List<QBDialog> chatDialogsList = QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                bundle);
        return chatDialogsList;
    }

    public List<QBChatHistoryMessage> getDialogMessages(QBDialog dialog,
            long lastDateLoad) throws QBResponseException {
        Bundle bundle = new Bundle();
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(ConstsCore.DIALOG_MESSAGES_PER_PAGE);
        if (lastDateLoad != ConstsCore.ZERO_LONG_VALUE) {
            customObjectRequestBuilder.gt(com.quickblox.chat.Consts.MESSAGE_DATE_SENT,
                    lastDateLoad);
        } else {
            deleteMessagesByDialogId(dialog.getDialogId());
        }
        List<QBChatHistoryMessage> dialogMessagesList = QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, bundle);

        if (dialogMessagesList != null) {
            DatabaseManager.saveChatMessages(context, dialogMessagesList, dialog.getDialogId());
        }

        return dialogMessagesList;
    }

    private void deleteMessagesByDialogId(String dialogId) {
        DatabaseManager.deleteMessagesByDialogId(context, dialogId);
    }

    private void deleteDialogLocal(String dialogId) {
        DatabaseManager.deleteDialogByDialogId(context, dialogId);
    }

    public void deleteDialog(String dialogId, QBDialogType dialogType) {
        try {
            if (QBDialogType.PRIVATE.equals(dialogType)) {
                QBPrivateChatManager.deleteDialog(dialogId);
            } else {
                QBGroupChatManager.deleteDialog(dialogId);
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        deleteDialogLocal(dialogId);
    }

    private class ChatConnectionListener implements ConnectionListener {

        @Override
        public void connected(XMPPConnection connection) {

        }

        @Override
        public void authenticated(XMPPConnection connection) {

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
