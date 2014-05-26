package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChat;
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
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.ChatMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class QBChatHelper extends BaseHelper implements QBMessageListener<QBChat>, QBPrivateChatManagerListener, QBRoomChatManagerListener {

    private QBRoomChat roomChat;
    private QBRoomChatManager roomChatManager;
    private QBUser user;
    private QBChatService chatService;
    private QBPrivateChat privateChat;
    private QBPrivateChatManager privateChatManager;
    private int privateChatId;
    private String groupChatName;
    private String opponentName;
    private String membersIDs = "";

    public QBChatHelper(Context context) {
        super(context);
    }

    public void sendPrivateMessage(String message) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessage(message);
        privateChat.sendMessage(chatMessage);

        saveMessageToCache(new ChatMessageCache(chatMessage.getBody(), user.getId(), String.valueOf(
                privateChatId), Consts.EMPTY_STRING, opponentName, null));
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    public void saveMessageToCache(ChatMessageCache chatMessageCache) {
        DatabaseManager.saveChatMessage(context, chatMessageCache);
    }

    public void sendGroupMessage(String message) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessage(message);
        roomChat.sendMessage(chatMessage);
        saveGroupMessageToCache(chatMessage, user.getId(), groupChatName, membersIDs);
    }

    public void sendPrivateMessageWithAttachImage(
            QBFile qbFile) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessageWithImage(qbFile);
        privateChat.sendMessage(chatMessage);
        saveMessageToCache(new ChatMessageCache(Consts.EMPTY_STRING, user.getId(), String.valueOf(
                privateChatId), qbFile.getPublicUrl(), opponentName, null));
    }


    private void saveGroupMessageToCache(QBChatMessage chatMessage, int senderId, String groupId,
            String membersIds) {
        DatabaseManager.saveChatMessage(context, new ChatMessageCache(chatMessage.getBody(), senderId,
                groupId, null, null, membersIds));
    }

    private QBChatMessage getQBChatMessageWithImage(QBFile qbFile) {
        QBChatMessage chatMessage = new QBChatMessage();
        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        return chatMessage;
    }

    @Override
    public void processMessage(QBChat chat, QBChatMessage chatMessage) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        String extraChatMessage = "";
        Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
        String fullname = friend.getFullname();
        if (TextUtils.isEmpty(messageBody)) {
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, fullname);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        String attachURL = "";
        if (TextUtils.isEmpty(messageBody)) {
            attachURL = getAttachUrlFromQBChatMessage(chatMessage);
        } else {
            attachURL = Consts.EMPTY_STRING;
        }

        if (chat instanceof QBRoomChat) {
            saveMessageToCache(new ChatMessageCache(messageBody, chatMessage.getSenderId(),
                    ((QBRoomChat) chat).getName(), null, null, membersIDs));
        } else {
            saveMessageToCache(new ChatMessageCache(messageBody, chatMessage.getSenderId(), String.valueOf(
                    friend.getId()), attachURL, fullname, null));
        }
    }

    private String getMessageBody(QBChatMessage chatMessage) {
        String messageBody = chatMessage.getBody();
        if (TextUtils.isEmpty(messageBody)) {
            messageBody = Consts.EMPTY_STRING;
        }
        return messageBody;
    }

    private String getAttachUrlFromQBChatMessage(QBChatMessage chatMessage) {
        List<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(chatMessage.getAttachments());
        if (!attachmentsList.isEmpty()) {
            return attachmentsList.get(attachmentsList.size() - 1).getUrl();
        }
        return Consts.EMPTY_STRING;
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

    public void init() {
        chatService = QBChatService.getInstance();
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        roomChatManager = chatService.getRoomChatManager();
        roomChatManager.addRoomChatManagerListener(this);
    }

    public void createPrivateChat(int opponentId) {
        Friend opponent = DatabaseManager.getFriendById(context, opponentId);
        privateChat = privateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
        opponentName = opponent.getFullname();
    }

    public void createOrJoinRoomChat(String roomName,
            List<Friend> friendList) throws SmackException.NotConnectedException, XMPPException, SmackException.NoResponseException {
        QBRoomChat roomChat = roomChatManager.getRoom(roomName);
        if (roomChat == null) {
            /*
            roomChat = roomChatManager.createRoom(roomName);
            roomChat.join();
            */
        }
        roomChat.addRoomUser(user.getId());
        for (Friend friend : friendList) {
            if (roomChat == null) {
                roomChat.addRoomUser(friend.getId());
            }
        }
        groupChatName = roomName;
    }

    public QBFile loadAttachFile(File file) {
        QBFile qbFile = null;
        try {
            qbFile = QBContent.uploadFileTask(file, true, (String) null);
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        return qbFile;
    }

    public void login(QBUser user) throws XMPPException, IOException, SmackException {
        if (!QBChatService.isInitialized()) {
            QBChatService.init(context);
        }
        if (!QBChatService.getInstance().isLoggedIn()) {
            QBChatService.getInstance().login(user);
            this.user = user;
        }
    }

    public void logout() throws QBResponseException, SmackException.NotConnectedException {
        chatService.logout();
    }

    public void destroy() {
        chatService.destroy();
    }

    public boolean isLoggedIn() {
        return chatService.isLoggedIn();
    }
}