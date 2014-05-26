package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
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
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.PrivateChatMessageCache;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
    private String membersIDs = Consts.EMPTY_STRING;
    private List<QBDialog> chatsDialogsList;

    public QBChatHelper(Context context) {
        super(context);
        chatsDialogsList = Collections.emptyList();
    }

    public void sendPrivateMessage(String message) {
        QBChatMessage chatMessage = getQBChatMessage(message);
        try {
            privateChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            ErrorUtils.logError(e);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
        }
        saveMessageToCache(new PrivateChatMessageCache(chatMessage.getBody(), user.getId(), String.valueOf(
                privateChatId), Consts.EMPTY_STRING, opponentName, null));
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    public void saveMessageToCache(PrivateChatMessageCache privateChatMessageCache) {
        DatabaseManager.saveChatMessage(context, privateChatMessageCache);
    }

    public void sendGroupMessage(String message) {
        QBChatMessage chatMessage = getQBChatMessage(message);
        try {
            roomChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            ErrorUtils.logError(e);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
            //TODO: SS reconnect
        }

        saveGroupMessageToCache(chatMessage, user.getId(), groupChatName, membersIDs);
    }

    private void saveGroupMessageToCache(QBChatMessage chatMessage, int senderId, String groupId,
            String membersIds) {
        DatabaseManager.saveChatMessage(context, new PrivateChatMessageCache(chatMessage.getBody(), senderId,
                groupId, null, null, membersIds));
    }

    public void sendPrivateMessageWithAttachImage(QBFile qbFile) {
        QBChatMessage chatMessage = getQBChatMessageWithImage(qbFile);
        try {
            privateChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            ErrorUtils.logError(e);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
        }
        saveMessageToCache(new PrivateChatMessageCache(Consts.EMPTY_STRING, user.getId(), String.valueOf(
                privateChatId), qbFile.getPublicUrl(), opponentName, null));
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
        String extraChatMessage;
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

        String attachURL;
        if (TextUtils.isEmpty(messageBody)) {
            attachURL = getAttachUrlFromQBChatMessage(chatMessage);
        } else {
            attachURL = Consts.EMPTY_STRING;
        }

        if (chat instanceof QBRoomChat) {
            saveMessageToCache(new PrivateChatMessageCache(messageBody, chatMessage.getSenderId(),
                    ((QBRoomChat) chat).getName(), null, null, membersIDs));
        } else {
            saveMessageToCache(new PrivateChatMessageCache(messageBody, chatMessage.getSenderId(),
                    String.valueOf(friend.getId()), attachURL, fullname, null));
        }
        updateLoadedChatDialog(chatMessage.getSenderId(), extraChatMessage);
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

    public void initPrivateChat(int opponentId) {
        Friend opponent = DatabaseManager.getFriendById(context, opponentId);
        privateChat = privateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
        opponentName = opponent.getFullname();
    }

    public void initRoomChat(String roomName, List<Friend> friendList) {
        if (roomChat == null) {
            roomChat = roomChatManager.createRoom(roomName);
        } else if (roomChatManager.getRoom(roomName) == null) {
            roomChat = roomChatManager.createRoom(roomName);
        } else if (roomChatManager.getRoom(roomName) != null) {
            roomChat = roomChatManager.getRoom(roomName);
        }
        String membersNames = Consts.EMPTY_STRING;
        try {
            roomChat.join();
            roomChat.addRoomUser(user.getId());
            for (Friend friend : friendList) {
                if (roomChat == null) {
                    roomChat.addRoomUser(Integer.valueOf(friend.getId()));
                }

                if (friend != null) {
                    membersIDs = membersIDs + friend.getId() + ",";
                    membersNames = membersNames + friend.getFullname() + ",";
                }
            }
        } catch (Exception e) {
            ErrorUtils.showError(context, e);
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

    public void login(QBUser user) {
        try {
            if (!QBChatService.isInitialized()) {
                QBChatService.init(context);
            }
            if (!QBChatService.getInstance().isLoggedIn()) {
                QBChatService.getInstance().login(user);
                this.user = user;
            }
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public void logout() throws QBResponseException {
        try {
            QBChatService.getInstance().logout();
        } catch (SmackException.NotConnectedException e) {
            throw new QBResponseException(e.getMessage());
        }
    }

    public void destroy() {
        QBChatService.getInstance().destroy();
    }

    public boolean isLoggedIn() {
        return chatService.isLoggedIn();
    }

    public List<QBDialog> getChatsDialogs() {
        Bundle bundle = new Bundle();
        try {
            QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
            customObjectRequestBuilder.setPagesLimit(Consts.CHATS_DIALOGS_PER_PAGE);
            chatsDialogsList = QBChatService.getChatDialogs(null, customObjectRequestBuilder, bundle);
        } catch (QBResponseException e) {
            e.printStackTrace();
        }
        return chatsDialogsList;
    }

    public List<QBDialog> getLoadedChatsDialogs() {
        return chatsDialogsList;
    }

    public void updateLoadedChatDialog(int occupantId, String lastMessage) {
        for(int i = 0; i< chatsDialogsList.size(); i++) {
            String dialogId = ChatUtils.getPrivateDialogIdByOccupantId(chatsDialogsList, occupantId);
            if(dialogId == null) {
                // TODo SF only for private chat
                addNewChatDialog(occupantId, lastMessage, QBDialogType.PRIVATE);
            }
            if(dialogId.equals(chatsDialogsList.get(i).getDialogId())) {
                chatsDialogsList.get(i).setLastMessage(lastMessage);
                break;
            }
        }
    }

    private void addNewChatDialog(int occupantId, String lastMessage, QBDialogType type) {
        QBDialog newDialog = new QBDialog();
        newDialog.setId(occupantId);
        newDialog.setLastMessage(lastMessage);
        ArrayList occupantsIdsList = new ArrayList<Integer>();
        occupantsIdsList.add(App.getInstance().getUser().getId());
        occupantsIdsList.add(occupantId);
        newDialog.setOccupantsIds(occupantsIdsList);
        newDialog.setUnreadMessageCount(Consts.ZERO_VALUE);
        newDialog.setType(type);
        chatsDialogsList.add(newDialog);
    }
}