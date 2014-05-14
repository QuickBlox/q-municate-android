package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;

public class QBChatHelper implements QBMessageListener<QBPrivateChat>, QBPrivateChatManagerListener {

    private static QBChatHelper instance;

    private Context context;
    private QBUser user;
    private QBChatService chatService;
    private QBPrivateChat privateChat;
    private QBPrivateChatManager privateChatManager;
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
        QBChatMessage chatMessage = getQBChatMessage(message);
        try {
            privateChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        saveMessageToCache(chatMessage, user.getId(), privateChatId, "");
    }

    public void sendPrivateMessageWithAttachFile(QBFile qbFile) {
        QBChatMessage chatMessage = getQBChatMessage(qbFile);
        saveMessageToCache(chatMessage, user.getId(), privateChatId, qbFile.getPublicUrl());
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    private QBChatMessage getQBChatMessage(QBFile qbFile) {
        QBChatMessage chatMessage = new QBChatMessage();
        QBAttachment attachment = new QBAttachment();
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        return chatMessage;
    }

    public void saveMessageToCache(QBChatMessage chatMessage, int senderId, int chatId, String attachUrl) {
        DatabaseManager.savePrivateChatMessage(context, chatMessage, senderId, chatId, attachUrl);
    }

    @Override
    public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        saveMessageToCache(chatMessage, chatMessage.getSenderId(), chatMessage.getSenderId(), "");
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, chatMessage.getBody());
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, DatabaseManager.getFriendFromCursor(
                DatabaseManager.getCursorFriendById(context, chatMessage.getSenderId())).getFullname());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public void chatCreated(QBPrivateChat privateChat, boolean createdLocally) {
        privateChat.addMessageListener(this);
    }

    public void initChats(Context context) {
        this.context = context;
        chatService = QBChatService.getInstance();
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
    }

    public void initPrivateChat(int opponentId) {
        user = App.getInstance().getUser();
        privateChat = privateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
    }

    public QBFile loadAttachFile(File file) {
        QBFile qbFile = null;
        try {
            qbFile = QBContent.uploadFileTask(file, true, (String) null);
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
        return qbFile;
    }
}