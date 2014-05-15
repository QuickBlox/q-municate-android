package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import android.util.Log;
import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.*;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.listeners.QBRoomChatManagerListener;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.service.QBServiceConsts;

import com.quickblox.qmunicate.model.Friend;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;

public class QBChatHelper implements QBMessageListener<QBPrivateChat>, QBPrivateChatManagerListener, QBRoomChatManagerListener {

    private static QBChatHelper instance;

    private Context context;
    private QBRoomChat qbRoomChat;
    private QBRoomChatManager qbRoomChatManager;
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
        saveMessageToCache(chatMessage, user.getId(), privateChatId);
    }

    public void sendGroupMessage(String message){
        Log.i("GroupMessage: ", "From sendGroup, Chat message: " + message);
        QBChatMessage qbChatMessage = getQBChatMessage(message);
        try {
            qbRoomChat.sendMessage(qbChatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        Log.i("GroupMessage: ", " Chat ID: " + groupChatName);
        saveGroupMessageToCache(qbChatMessage, user.getId(), groupChatName);
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        return chatMessage;
    }

    private void saveMessageToCache(QBChatMessage chatMessage, int senderId, int chatId) {
        DatabaseManager.savePrivateChatMessage(context, chatMessage, senderId, chatId);
    }

    private void saveGroupMessageToCache(QBChatMessage qbChatMessage, int senderId, String groupId){
        DatabaseManager.saveGroupChatMessage(context, qbChatMessage, senderId, groupId);
    }

    @Override
    public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        saveMessageToCache(chatMessage, chatMessage.getSenderId(), chatMessage.getSenderId());
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
        qbRoomChatManager = chatService.getRoomChatManager();
        qbRoomChatManager.addRoomChatManagerListener(this);
    }

    public void initPrivateChat(int opponentId) {
        user = App.getInstance().getUser();
        privateChat = privateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
    }

    public void initRoomChat(Context context, String roomName, List<Friend> friends){
        this.context = context;
        user = App.getInstance().getUser();
        qbRoomChat = qbRoomChatManager.createRoom(roomName);
        try{
            qbRoomChat.join();
            qbRoomChat.addRoomUser(user.getId());
            for(Friend friend : friends){
                qbRoomChat.addRoomUser(Integer.valueOf(friend.getId()));
            }


        } catch (Exception e){
            e.printStackTrace();
        }
        Log.i("GroupMessage: ", "From initRoomChat, RoomChat: " + qbRoomChat);
        groupChatName = roomName;
    }
}