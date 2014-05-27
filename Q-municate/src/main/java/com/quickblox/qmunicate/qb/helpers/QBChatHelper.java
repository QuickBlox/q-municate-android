package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

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
import com.quickblox.qmunicate.model.ChatMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QBChatHelper extends BaseHelper implements QBPrivateChatManagerListener {

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_ROOM_NAME = "name";
    public static final String PROPERTY_DIALOG_TYPE_CODE = "type";
    public static final String PROPERTY_DIALOG_ID = "_id";
    public static final String PROPERTY_ROOM_JID = "room_jid";

    private static final String TAG = QBChatHelper.class.getSimpleName();
    private static final String OCCUPANT_IDS_DIVIDER = ",";

    private QBChatService chatService;
    private QBUser user;

    private QBPrivateChatManager privateChatManager;
    private QBPrivateChat privateChat;
    private int privateChatId;

    private QBRoomChatManager roomChatManager;
    private QBRoomChat roomChat;
    private String opponentName;
    private List<QBDialog> chatsDialogsList;
    private int counterUnreadChatsDialogs;

    private PrivateChatMessageListener privateChatMessageListener = new PrivateChatMessageListener();
    private RoomChatMessageListener roomChatMessageListener = new RoomChatMessageListener();

    public QBChatHelper(Context context) {
        super(context);
        chatsDialogsList = Collections.emptyList();
    }

    public void sendPrivateMessage(
            String message) throws XMPPException, SmackException.NotConnectedException {
        QBChatMessage chatMessage = getQBChatMessage(message);
        privateChat.sendMessage(chatMessage);

        saveMessageToCache(new ChatMessageCache(chatMessage.getBody(), user.getId(), privateChatId,
                Consts.EMPTY_STRING));
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

    @Override
    public void chatCreated(QBPrivateChat privateChat, boolean createdLocally) {
        privateChat.addMessageListener(privateChatMessageListener);
    }

    public void init() {
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        roomChatManager = chatService.getRoomChatManager();
    }

    public void createPrivateChat(int opponentId) {
        Friend opponent = DatabaseManager.getFriendById(context, opponentId);
        privateChat = privateChatManager.createChat(opponentId, privateChatMessageListener);
        privateChatId = opponentId;
        opponentName = opponent.getFullname();
    }

    public QBDialog createRoomChat(String roomName,
            List<Integer> friendIdsList) throws SmackException, XMPPException, QBResponseException {
        ArrayList<Integer> occupantIdsList = getOccupantIdsList(friendIdsList);
        QBDialog dialog = roomChatManager.createDialog(roomName, QBDialogType.GROUP, occupantIdsList);
        joinRoomChat(dialog.getRoomJid());
        inviteFriendsToRoom(dialog, friendIdsList);
        chatsDialogsList.add(dialog);
        return dialog;
    }

    private ArrayList<Integer> getOccupantIdsList(List<Integer> friendIdsList) {
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>(friendIdsList);
        occupantIdsList.add(user.getId());
        return occupantIdsList;
    }

    private void inviteFriendsToRoom(QBDialog dialog,
            List<Integer> friendIdsList) throws XMPPException, SmackException {
        for (Integer friendId : friendIdsList) {
            notifyFriendAboutInvitation(dialog, friendId);
        }
    }

    private void notifyFriendAboutInvitation(QBDialog dialog,
            Integer friendId) throws XMPPException, SmackException {
        QBPrivateChat chat = privateChatManager.createChat(friendId, new PrivateChatMessageListener());
        QBChatMessage message = createRoomNotificationMessage(dialog);
        chat.sendMessage(message);
        Log.d(TAG, "friend notified id=" + friendId);
    }

    private QBChatMessage createRoomNotificationMessage(QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = occupantIdsToStringFromDialog(dialog);
        String dialogName = dialog.getName();
        String dialogTypeCode = String.valueOf(dialog.getType().ordinal());

        QBChatMessage message = new QBChatMessage();
        message.setProperty(PROPERTY_DIALOG_ID, dialogId);
        message.setProperty(PROPERTY_ROOM_JID, roomJid);
        message.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        message.setProperty(PROPERTY_ROOM_NAME, dialogName);
        message.setProperty(PROPERTY_DIALOG_TYPE_CODE, dialogTypeCode);
        return message;
    }

    private String occupantIdsToStringFromDialog(QBDialog dialog) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, dialog.getOccupants());
    }

    public void joinRoomChat(String jid) throws XMPPException, SmackException {
        roomChat = roomChatManager.getRoom(jid);
        if (roomChat == null) {
            roomChat = roomChatManager.createRoom(jid);
            roomChat.addMessageListener(roomChatMessageListener);
            roomChat.join();
        }
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
        chatsDialogsList.clear();
        chatService.destroy();
    }

    public boolean isLoggedIn() {
        return chatService.isLoggedIn();
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

    private String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = Consts.EMPTY_STRING;
        if (TextUtils.isEmpty(chatMessage.getBody())) {
            attachURL = ChatUtils.getAttachUrlFromQBChatMessage(chatMessage);
        }
        return attachURL;
    }

    public List<QBDialog> getChatsDialogs() throws QBResponseException {
        Bundle bundle = new Bundle();
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        customObjectRequestBuilder.setPagesLimit(Consts.CHATS_DIALOGS_PER_PAGE);
        chatsDialogsList = QBChatService.getChatDialogs(null, customObjectRequestBuilder, bundle);
        initCounterUnreadChatsDialogs();
        return chatsDialogsList;
    }

    public List<QBDialog> getLoadedChatsDialogs() {
        return chatsDialogsList;
    }

    public void updateLoadedChatDialog(int occupantId, String lastMessage, int unreadMessages) {
        for (int i = 0; i < chatsDialogsList.size(); i++) {
            String dialogId = ChatUtils.getPrivateDialogIdByOccupantId(chatsDialogsList, occupantId);
            if (dialogId == null) {
                // TODo SF only for private chat
                addNewChatDialog(occupantId, lastMessage, QBDialogType.PRIVATE);
            }
            if (dialogId.equals(chatsDialogsList.get(i).getDialogId())) {
                chatsDialogsList.get(i).setLastMessage(lastMessage);
                if(unreadMessages != -1) {
                    chatsDialogsList.get(i).setUnreadMessageCount(unreadMessages);
                    counterUnreadChatsDialogs--;
                }
                break;
            }
        }
    }

    private void addNewChatDialog(int occupantId, String lastMessage, QBDialogType type) {
        QBDialog newDialog = new QBDialog(occupantId + Consts.EMPTY_STRING);
        newDialog.setLastMessage(lastMessage);
        ArrayList occupantsIdsList = new ArrayList<Integer>();
        occupantsIdsList.add(user.getId());
        occupantsIdsList.add(occupantId);
        newDialog.setOccupantsIds(occupantsIdsList);
        newDialog.setUnreadMessageCount(Consts.ZERO_VALUE);
        newDialog.setType(type);
        chatsDialogsList.add(newDialog);
    }

    private void processIfRoomNotificationMessage(Friend sender, QBChatMessage chatMessage) {
        if (isNotificationMessage(chatMessage)) {
            QBDialog dialog = parseDialogFromMessage(chatMessage);
            tryJoinRoomChat(dialog.getRoomJid());
            chatsDialogsList.add(dialog);
            String message = context.getResources().getString(R.string.user_created_room,
                    sender.getFullname(), dialog.getName());
            chatMessage.setBody(message);
        }
    }

    private void tryJoinRoomChat(String roomJid) {
        try {
            joinRoomChat(roomJid);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    private QBDialog parseDialogFromMessage(QBChatMessage chatMessage) {
        String dialogId = chatMessage.getProperty(PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String dialogTypeCode = chatMessage.getProperty(PROPERTY_DIALOG_TYPE_CODE);

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setOccupantsIds(parseOccupantIdsFromString(occupantsIds));
        dialog.setName(dialogName);
        dialog.setType(QBDialogType.parseByCode(Integer.parseInt(dialogTypeCode)));
        return dialog;
    }

    private boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_DIALOG_ID) != null;
    }

    private ArrayList<Integer> parseOccupantIdsFromString(String occupantIds) {
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        String[] occupantIdsArray = occupantIds.split(OCCUPANT_IDS_DIVIDER);
        for (String occupantId : occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId));
        }
        return occupantIdsList;
    }

    private class PrivateChatMessageListener implements QBMessageListener<QBPrivateChat> {

        @Override
        public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
            Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
            processIfRoomNotificationMessage(friend, chatMessage);
            String attachURL = getAttachUrlIfExists(chatMessage);
            saveMessageToCache(new ChatMessageCache(chatMessage.getBody(), chatMessage.getSenderId(),
                    chatMessage.getSenderId(), attachURL));
            notifyMessageReceived(chatMessage, friend);
        }
    }

    private class RoomChatMessageListener implements QBMessageListener<QBRoomChat> {

        @Override
        public void processMessage(QBRoomChat roomChat, QBChatMessage chatMessage) {
            Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
            saveGroupMessageToCache(chatMessage, chatMessage.getSenderId(), roomChat.getJid());
            if (!chatMessage.getSenderId().equals(user.getId())) {
                // TODO IS handle logic when friend is not in the friend list
                notifyMessageReceived(chatMessage, friend);
            }
            updateLoadedChatDialog(chatMessage.getSenderId(), chatMessage.getBody(), -1);
        }
    }

    private void initCounterUnreadChatsDialogs() {
        for (QBDialog dialog: chatsDialogsList) {
            if(dialog.getUnreadMessageCount() > Consts.ZERO_VALUE) {
                counterUnreadChatsDialogs++;
            }
        }
    }

    public int getCountUnreadChatsDialogs() {
        return counterUnreadChatsDialogs;
    }

    public List<QBHistoryMessage> getDialogMessages(QBDialog dialog) throws QBResponseException {
        Bundle bundle = new Bundle();
        QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
        customObjectRequestBuilder.setPagesLimit(Consts.DIALOG_MESSAGES_PER_PAGE);
        return QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, bundle);
    }
}