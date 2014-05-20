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
import com.quickblox.module.chat.QBRoomChat;
import com.quickblox.module.chat.QBRoomChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.listeners.QBRoomChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBServiceConsts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QBChatHelper implements QBMessageListener<QBPrivateChat>, QBPrivateChatManagerListener, QBRoomChatManagerListener {

    private static QBChatHelper instance;

    private Context context;
    private QBRoomChat roomChat;
    private QBRoomChatManager roomChatManager;
    private QBUser user;
    private QBChatService chatService;
    private QBPrivateChat privateChat;
    private QBPrivateChatManager privateChatManager;
    private int privateChatId;
    private String groupChatName;

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

    public void sendGroupMessage(String message) {
        QBChatMessage chatMessage = getQBChatMessage(message);
        try {
            roomChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            //TODO: reconnect
        }
        saveGroupMessageToCache(chatMessage, user.getId(), groupChatName);
    }

    private void saveGroupMessageToCache(QBChatMessage chatMessage, int senderId, String groupId) {
        DatabaseManager.saveGroupChatMessage(context, chatMessage, senderId, groupId);
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
        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        return chatMessage;
    }

    @Override
    public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        // TODO SF "file was attached" to Model
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, TextUtils.isEmpty(
                messageBody) ? "file was attached" : messageBody);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, DatabaseManager.getFriend(context,
                chatMessage.getSenderId()).getFullname());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        saveMessageToCache(messageBody, chatMessage.getSenderId(), chatMessage.getSenderId(),
                TextUtils.isEmpty(messageBody) ? getAttachUrlFromQBChatMessage(chatMessage) : "");
    }

    private String getMessageBody(QBChatMessage chatMessage) {
        String messageBody = chatMessage.getBody();
        if (TextUtils.isEmpty(messageBody)) {
            messageBody = "";
        }
        return messageBody;
    }

    private String getAttachUrlFromQBChatMessage(QBChatMessage chatMessage) {
        List<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(chatMessage.getAttachments());
        if (!attachmentsList.isEmpty()) {
            return attachmentsList.get(attachmentsList.size() - 1).getUrl();
        }
        return "";
    }

    @Override
    public void chatCreated(QBPrivateChat privateChat, boolean createdLocally) {
        privateChat.addMessageListener(this);
    }

    @Override
    public void roomCreated(QBRoomChat qbRoomChat) {

    }

    @Override
    public void roomJoined(QBRoomChat qbRoomChat) {

    }

    @Override
    public void onError(List<String> strings) {

    }

    public void initChats(Context context) {
        this.context = context;
        chatService = QBChatService.getInstance();
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        roomChatManager = chatService.getRoomChatManager();
        roomChatManager.addRoomChatManagerListener(this);
    }

    public void initPrivateChat(int opponentId) {
        user = App.getInstance().getUser();
        privateChat = privateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
    }

    public void initRoomChat(Context context, String roomName, List<Friend> friends) {
        this.context = context;
        user = App.getInstance().getUser();
        roomChat = roomChatManager.createRoom(roomName);
        try {
            roomChat.join();
            roomChat.addRoomUser(user.getId());
            for (Friend friend : friends) {
                roomChat.addRoomUser(Integer.valueOf(friend.getId()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        groupChatName = roomName;
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