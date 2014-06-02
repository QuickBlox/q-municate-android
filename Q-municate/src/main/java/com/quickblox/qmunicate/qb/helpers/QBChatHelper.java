package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBHistoryMessage;
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
import com.quickblox.qmunicate.model.DialogMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;
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
    private QBRoomChatManager roomChatManager;
    private QBRoomChat roomChat;
    private PrivateChatMessageListener privateChatMessageListener = new PrivateChatMessageListener();
    private RoomChatMessageListener roomChatMessageListener = new RoomChatMessageListener();
    private int opponentId;

    public QBChatHelper(Context context) {
        super(context);
    }

    public void sendPrivateMessage(
            String message) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessage(message);
        privateChat.sendMessage(chatMessage);

        String roomJidId = opponentId + Consts.EMPTY_STRING;
        String attachUrl = Consts.EMPTY_STRING;
        long time = DateUtils.getCurrentTime();

        saveMessageToCache(new DialogMessageCache(roomJidId, user.getId(), chatMessage.getBody(), attachUrl, time));
    }

    public void saveMessageToCache(DialogMessageCache dialogMessageCache) {
        DatabaseManager.saveChatMessage(context, dialogMessageCache);
    }

    public void sendGroupMessage(String message) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessage(message);
        roomChat.sendMessage(chatMessage);
    }

    public void sendGroupMessageWithAttachImage(
            QBFile file) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessageWithImage(file);
        roomChat.sendMessage(chatMessage);
    }

    public void sendPrivateMessageWithAttachImage(
            QBFile file) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessageWithImage(file);
        privateChat.sendMessage(chatMessage);

        String roomJidId = opponentId + Consts.EMPTY_STRING;
        String attachUrl = file.getPublicUrl();
        long time = DateUtils.getCurrentTime();

        saveMessageToCache(new DialogMessageCache(roomJidId, user.getId(), chatMessage.getBody(), attachUrl, time));
    }

    @Override
    public void chatCreated(QBPrivateChat privateChat, boolean createdLocally) {
        privateChat.addMessageListener(privateChatMessageListener);
    }

    public void updateDialog(QBDialog dialog, String roomJidId) {
        saveDialogToCache(dialog, roomJidId);
    }

    public void init() {
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        roomChatManager = chatService.getRoomChatManager();
    }

    public void createPrivateChat(int opponentId) {
        privateChat = privateChatManager.createChat(opponentId, privateChatMessageListener);
        this.opponentId = opponentId;
    }

    public QBDialog createRoomChat(String roomName,
                                   List<Integer> friendIdsList) throws SmackException, XMPPException, QBResponseException {
        ArrayList<Integer> occupantIdsList = ChatUtils.getOccupantIdsWithUser(friendIdsList);
        QBDialog dialog = roomChatManager.createDialog(roomName, QBDialogType.GROUP, occupantIdsList);
        joinRoomChat(dialog.getRoomJid());
        inviteFriendsToRoom(dialog, friendIdsList);
        saveDialogToCache(dialog, dialog.getRoomJid());
        return dialog;
    }

    public void joinRoomChat(String roomJidId) throws XMPPException, SmackException {
        roomChat = roomChatManager.getRoom(roomJidId);
        if (roomChat == null) {
            roomChat = roomChatManager.createRoom(roomJidId);
            roomChat.addMessageListener(roomChatMessageListener);
            roomChat.join();
        }
    }

    public QBFile loadAttachFile(File inputFile) {
        QBFile file = null;
        try {
            file = QBContent.uploadFileTask(inputFile, true, (String) null);
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        return file;
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

    public List<QBDialog> getDialogs() throws QBResponseException {
        Bundle bundle = new Bundle();
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        customObjectRequestBuilder.setPagesLimit(Consts.CHATS_DIALOGS_PER_PAGE);
        List<QBDialog> chatDialogsList = QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                bundle);
        // TODO SF now not used.
        //        deleteDialogs();
        saveDialogsToCache(chatDialogsList);
        return chatDialogsList;
    }

    public List<QBHistoryMessage> getDialogMessages(QBDialog dialog,
                                                    String roomJidId) throws QBResponseException {
        Bundle bundle = new Bundle();
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        customObjectRequestBuilder.setPagesLimit(Consts.DIALOG_MESSAGES_PER_PAGE);
        List<QBHistoryMessage> dialogMessagesList = QBChatService.getDialogMessages(dialog,
                customObjectRequestBuilder, bundle);
        if(dialogMessagesList != null) {
            deleteMessagesByRoomJidId(roomJidId);
            saveChatMessagesToCache(dialogMessagesList, roomJidId);
        }
        return dialogMessagesList;
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    private QBChatMessage getQBChatMessageWithImage(QBFile qbFile) {
        QBChatMessage chatMessage = new QBChatMessage();
        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        return chatMessage;
    }

    private void inviteFriendsToRoom(QBDialog dialog,
                                     List<Integer> friendIdsList) throws XMPPException, SmackException {
        for (Integer friendId : friendIdsList) {
            notifyFriendAboutInvitation(dialog, friendId);
        }
    }

    private void saveDialogToCache(QBDialog dialog, String roomJidId) {
        DatabaseManager.saveDialog(context, dialog, roomJidId);
    }

    private void notifyFriendAboutInvitation(QBDialog dialog,
                                             Integer friendId) throws XMPPException, SmackException {
        QBPrivateChat chat = privateChatManager.createChat(friendId, privateChatMessageListener);
        QBChatMessage message = ChatUtils.createRoomNotificationMessage(dialog);
        chat.sendMessage(message);
    }

    private void saveDialogsToCache(List<QBDialog> dialogsList) {
        DatabaseManager.saveDialogs(context, dialogsList);
    }

    private void deleteMessagesByRoomJidId(String roomJidId) {
        DatabaseManager.deleteMessagesByRoomJidId(context, roomJidId);
    }

    private void saveChatMessagesToCache(List<QBHistoryMessage> dialogMessagesList, String roomJidId) {
        DatabaseManager.saveChatMessages(context, dialogMessagesList, roomJidId);
    }

    private void deleteDialogs() {
        DatabaseManager.deleteAllDialogs(context);
    }

    private void notifyMessageReceived(QBChatMessage chatMessage, Friend friend) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        String extraChatMessage;

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

    private String getMessageBody(QBChatMessage chatMessage) {
        String messageBody = chatMessage.getBody();
        if (TextUtils.isEmpty(messageBody)) {
            messageBody = Consts.EMPTY_STRING;
        }
        return messageBody;
    }

    private String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = Consts.EMPTY_STRING;
        if (TextUtils.isEmpty(chatMessage.getBody())) {
            attachURL = ChatUtils.getAttachUrlFromMessage(
                    new ArrayList<QBAttachment>(chatMessage.getAttachments()));
        }
        return attachURL;
    }

    private void tryJoinRoomChat(String roomJid) {
        try {
            joinRoomChat(roomJid);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    private class PrivateChatMessageListener implements QBMessageListener<QBPrivateChat> {

        @Override
        public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
            Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());

            if (ChatUtils.isNotificationMessage(chatMessage)) {
                QBDialog dialog = ChatUtils.parseDialogFromMessage(chatMessage,
                        context.getResources().getString(R.string.user_created_room, friend.getFullname()));
                chatMessage.setBody(dialog.getLastMessage());
                tryJoinRoomChat(dialog.getRoomJid());
                saveDialogToCache(dialog, dialog.getRoomJid());
            }

            String attachUrl = getAttachUrlIfExists(chatMessage);
            String roomJidId = chatMessage.getSenderId() + Consts.EMPTY_STRING;
            long time = DateUtils.getCurrentTime();

            saveMessageToCache(new DialogMessageCache(roomJidId, chatMessage.getSenderId(), chatMessage.getBody(), attachUrl, time));
            notifyMessageReceived(chatMessage, friend);
        }
    }

    private class RoomChatMessageListener implements QBMessageListener<QBRoomChat> {

        @Override
        public void processMessage(QBRoomChat roomChat, QBChatMessage chatMessage) {
            Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
            String attachUrl = getAttachUrlIfExists(chatMessage);
            long time = DateUtils.getCurrentTime();
            saveMessageToCache(new DialogMessageCache(roomChat.getJid(), chatMessage.getSenderId(), chatMessage.getBody(), attachUrl, time));
            if (!chatMessage.getSenderId().equals(user.getId())) {
                // TODO IS handle logic when friend is not in the friend list
                notifyMessageReceived(chatMessage, friend);
            }
        }
    }
}