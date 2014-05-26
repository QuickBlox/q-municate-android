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
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
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

public class QBChatHelper extends BaseHelper implements QBPrivateChatManagerListener {

    private QBChatService chatService;
    private QBUser user;

    private QBPrivateChatManager privateChatManager;
    private QBPrivateChat privateChat;
    private int privateChatId;

    private QBRoomChatManager roomChatManager;
    private QBRoomChat roomChat;
    private String roomJid;
    private String opponentName;

    public QBChatHelper(Context context) {
        super(context);
    }

    public void sendPrivateMessage(
            String message) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessage(message);
        privateChat.sendMessage(chatMessage);

        saveMessageToCache(new ChatMessageCache(chatMessage.getBody(), user.getId(), String.valueOf(
                privateChatId), Consts.EMPTY_STRING));
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
        saveGroupMessageToCache(chatMessage, user.getId(), roomJid);
    }

    public void sendPrivateMessageWithAttachImage(
            QBFile qbFile) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessageWithImage(qbFile);
        privateChat.sendMessage(chatMessage);
        saveMessageToCache(new ChatMessageCache(Consts.EMPTY_STRING, user.getId(), String.valueOf(
                privateChatId), qbFile.getPublicUrl()));
    }

    private void saveGroupMessageToCache(QBChatMessage chatMessage, int senderId, String groupId) {
        DatabaseManager.saveChatMessage(context, new ChatMessageCache(chatMessage.getBody(), senderId,
                groupId, null));
    }

    private QBChatMessage getQBChatMessageWithImage(QBFile qbFile) {
        QBChatMessage chatMessage = new QBChatMessage();
        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        return chatMessage;
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
        privateChat.addMessageListener(new PrivateChatMessageListener());
    }

    public void init() {
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        roomChatManager = chatService.getRoomChatManager();
    }

    public void createPrivateChat(int opponentId) {
        Friend opponent = DatabaseManager.getFriendById(context, opponentId);
        privateChat = privateChatManager.createChat(opponentId, new PrivateChatMessageListener());
        privateChatId = opponentId;
        opponentName = opponent.getFullname();
    }

    public void createRoomChat(String roomName,
            ArrayList<Integer> friendIdsList) throws SmackException, XMPPException, QBResponseException {
        QBDialog dialog = roomChatManager.createDialog(roomName, QBDialogType.GROUP, friendIdsList);
        joinRoomChat(dialog.getRoomJid());
        for (Integer friendId : friendIdsList) {
            roomChat.addRoomUser(friendId);
        }
    }

    public void joinRoomChat(String jid) throws XMPPException, SmackException {
        roomChat = roomChatManager.createRoom(jid);
        roomChat.addMessageListener(new RoomChatMessageListener());
        roomChat.join();
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
            chatService = QBChatService.getInstance();
        }
        if (!chatService.isLoggedIn()) {
            chatService.login(user);
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

    private void notifyMessageReceived(QBChatMessage chatMessage, Friend friend) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        String extraChatMessage = "";

        String fullname = friend.getFullname();
        if (TextUtils.isEmpty(messageBody)) {
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, fullname);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = Consts.EMPTY_STRING;
        if (TextUtils.isEmpty(chatMessage.getBody())) {
            attachURL = getAttachUrlFromQBChatMessage(chatMessage);
        }
        return attachURL;
    }

    private class PrivateChatMessageListener implements QBMessageListener<QBPrivateChat> {

        @Override
        public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
            Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
            String attachURL = getAttachUrlIfExists(chatMessage);
            saveMessageToCache(new ChatMessageCache(chatMessage.getBody(), chatMessage.getSenderId(),
                    friend.getId(), attachURL));
            notifyMessageReceived(chatMessage, friend);
        }
    }

    private class RoomChatMessageListener implements QBMessageListener<QBRoomChat> {

        @Override
        public void processMessage(QBRoomChat roomChat, QBChatMessage chatMessage) {
            saveMessageToCache(new ChatMessageCache(chatMessage.getBody(), chatMessage.getSenderId(),
                    roomChat.getJid(), null));
        }
    }

    public List<QBDialog> getChatDialogs() {
        Bundle bundle = new Bundle();
        List<QBDialog> chatDialogs = Collections.emptyList();
        try {
            QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
            customObjectRequestBuilder.setPagesLimit(Consts.CHATS_DIALOGS_PER_PAGE);
            chatDialogs = QBChatService.getChatDialogs(null, customObjectRequestBuilder, bundle);
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
        return chatDialogs;
    }
}