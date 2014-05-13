package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;

import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.receiver.BroadcastActions;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

public class QBChatHelper implements QBMessageListener<QBPrivateChat>, QBPrivateChatManagerListener {

    private static QBChatHelper instance;

    private Context context;
    private QBUser qbUser;
    private QBChatService qbChatService;
    private QBPrivateChat qbPrivateChat;
    private QBPrivateChatManager qbPrivateChatManager;
    private int privateChatId;

    private QBChatHelper() {
        instance = this;
    }

    public static QBChatHelper getInstance() {
        if (instance == null) {
            return new QBChatHelper();
        }
        return instance;
    }

    public void sendPrivateMessage(String message) {
        QBChatMessage qbChatMessage = getQBChatMessage(message);
        try {
            qbPrivateChat.sendMessage(qbChatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        saveMessageToCache(qbChatMessage, qbUser.getId(), privateChatId);
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setBody(body);
        return qbChatMessage;
    }

    private void saveMessageToCache(QBChatMessage qbChatMessage, int senderId, int chatId) {
        DatabaseManager.savePrivateChatMessage(context, qbChatMessage, senderId, chatId);
    }

    @Override
    public void processMessage(QBPrivateChat qbPrivateChat, QBChatMessage message) {
        saveMessageToCache(message, message.getSenderId(), message.getSenderId());
        Intent intent = new Intent(BroadcastActions.GOT_MESSAGE);
        intent.putExtra(BroadcastActions.EXTRA_MESSAGE, message.getBody());
        context.sendBroadcast(intent);
    }

    @Override
    public void chatCreated(QBPrivateChat qbPrivateChat, boolean createdLocally) {
        if (!createdLocally) {
            this.qbPrivateChat.addMessageListener(this);
        }
    }

    public void initChats() {
        qbChatService = QBChatService.getInstance();
        qbPrivateChatManager = qbChatService.getPrivateChatManager();
        qbPrivateChatManager.addPrivateChatManagerListener(this);
    }

    public void initPrivateChat(Context context, int opponentId) {
        this.context = context;
        qbUser = App.getInstance().getUser();
        qbPrivateChat = qbPrivateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
    }
}