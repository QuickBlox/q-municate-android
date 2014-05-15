package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBAttachmentType;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        saveMessageToCache(chatMessage.getBody(), user.getId(), privateChatId, "");
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    public void saveMessageToCache(String message, int senderId, int chatId, String attachUrl) {
        DatabaseManager.savePrivateChatMessage(context, message, senderId, chatId, attachUrl);
    }

    public void sendPrivateMessageWithAttachImage(QBFile qbFile) {
        QBChatMessage chatMessage = getQBChatMessageWithImage(qbFile);
        try {
            privateChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        saveMessageToCache("", user.getId(), privateChatId, qbFile.getPublicUrl());
    }

    private QBChatMessage getQBChatMessageWithImage(QBFile qbFile) {
        QBChatMessage chatMessage = new QBChatMessage();
        QBAttachment attachment = new QBAttachment();
        attachment.setType(QBAttachmentType.photo.toString());
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        return chatMessage;
    }

    @Override
    public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, TextUtils.isEmpty(messageBody)? "attach file" : messageBody);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, DatabaseManager.getFriendFromCursor(
                DatabaseManager.getCursorFriendById(context, chatMessage.getSenderId())).getFullname());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        String att = getAttachUrlFromQBChatMessage(chatMessage);
        saveMessageToCache(messageBody, chatMessage.getSenderId(), chatMessage.getSenderId(), att);
    }

    private String getMessageBody(QBChatMessage chatMessage) {
        String messageBody = chatMessage.getBody();
        if (TextUtils.isEmpty(messageBody)) {
            messageBody = "";
        }
        return messageBody;
    }

    private String getAttachUrlFromQBChatMessage(QBChatMessage chatMessage) {
        List<QBAttachment> attachmentsList = null;
        try {
            attachmentsList = new ArrayList<QBAttachment>(chatMessage.getAttachments());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (attachmentsList.isEmpty()) {
            for (QBAttachment attachment : attachmentsList) {
                return attachment.getUrl();
            }
        }
        return "";
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