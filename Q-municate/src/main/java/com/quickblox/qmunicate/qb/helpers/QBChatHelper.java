package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import android.util.Log;
import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.*;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.listeners.QBRoomChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.model.PrivateChatMessageCache;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QBChatHelper implements QBMessageListener<QBChat>,  QBPrivateChatManagerListener, QBRoomChatManagerListener {

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
    private String opponentName;
    private String membersIDs = "";

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
        saveMessageToCache(new PrivateChatMessageCache(chatMessage.getBody(), user.getId(), privateChatId, Consts.EMPTY_STRING, opponentName));
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    public void saveMessageToCache(PrivateChatMessageCache privateChatMessageCache) {
        DatabaseManager.savePrivateChatMessage(context, privateChatMessageCache);
    }

    public void sendGroupMessage(String message) {
        QBChatMessage chatMessage = getQBChatMessage(message);
        try {
            roomChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
            //TODO: SS reconnect
        }

        saveGroupMessageToCache(chatMessage, user.getId(), groupChatName, membersIDs);
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
        saveMessageToCache(new PrivateChatMessageCache(Consts.EMPTY_STRING, user.getId(), privateChatId, qbFile.getPublicUrl(), opponentName));
    }


    private void saveGroupMessageToCache(QBChatMessage chatMessage, int senderId, String groupId, String membersIds){
        DatabaseManager.saveGroupChatMessage(context, chatMessage, senderId, groupId, membersIds);
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
        Friend friend = DatabaseManager.getFriendFromCursor(
                DatabaseManager.getCursorFriendById(context, chatMessage.getSenderId()));
        String fullname = friend.getFullname();
        if(TextUtils.isEmpty(messageBody)){
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }
        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, fullname);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        String attachURL = "";
        if(TextUtils.isEmpty(messageBody)){
            attachURL = getAttachUrlFromQBChatMessage(chatMessage);
        } else {
            attachURL = Consts.EMPTY_STRING;
        }
        Log.i("Message", "Processing... " + messageBody + "SenderID: " + chatMessage.getSenderId() + ", Opponent name: " + fullname);

        if(chat instanceof QBRoomChat){
            saveGroupMessageToCache(chatMessage, chatMessage.getSenderId(), ((QBRoomChat) chat).getName(), membersIDs);
        } else {
            saveMessageToCache(new PrivateChatMessageCache(messageBody, chatMessage.getSenderId(), friend.getId(),
                    attachURL, fullname));
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

    public void initChats(Context context) {
        this.context = context;
        chatService = QBChatService.getInstance();
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(this);
        roomChatManager = chatService.getRoomChatManager();
        roomChatManager.addRoomChatManagerListener(this);
    }

    public void initPrivateChat(int opponentId, String opponentName) {
        user = App.getInstance().getUser();
        privateChat = privateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
        this.opponentName = opponentName;
    }

    public void initRoomChat(Context context, String roomName, List<Friend> friendList) {
        this.context = context;
        user = App.getInstance().getUser();
        if(roomChat == null){
            roomChat = roomChatManager.createRoom(roomName);
        } else if(roomChatManager.getRoom(roomName) == null){
            roomChat = roomChatManager.createRoom(roomName);
        }else if(roomChatManager.getRoom(roomName) != null){

            roomChat = roomChatManager.getRoom(roomName);
        }
        String membersNames = "";
        try {
            roomChat.join();
            roomChat.addRoomUser(user.getId());
            for (Friend friend : friendList) {
                if(roomChat == null){
                    roomChat.addRoomUser(Integer.valueOf(friend.getId()));
                }

                if(friend != null){
                    membersIDs = membersIDs + friend.getId() + ",";
                    membersNames = membersNames + friend.getFullname() + ",";
                }
            }

            Log.i("ChatName﹕", membersNames + " while formed.");
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