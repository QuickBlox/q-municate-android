package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;

import android.util.Log;
import com.quickblox.internal.core.helper.Lo;
import com.quickblox.module.chat.*;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.listeners.QBRoomChatManagerListener;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.core.receiver.BroadcastActions;

import com.quickblox.qmunicate.model.Friend;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;

public class QBChatHelper implements QBMessageListener<QBPrivateChat>, QBPrivateChatManagerListener, QBRoomChatManagerListener {

    private static QBChatHelper instance;

    private Context context;
    private QBUser qbUser;
    private QBChatService qbChatService;
    private QBPrivateChat qbPrivateChat;
    private QBPrivateChatManager qbPrivateChatManager;
    private QBRoomChat qbRoomChat;
    private QBRoomChatManager qbRoomChatManager;
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
        QBChatMessage qbChatMessage = getQBChatMessage(message);
        try {
            qbPrivateChat.sendMessage(qbChatMessage);
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        saveMessageToCache(qbChatMessage, qbUser.getId(), privateChatId);
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
        saveGroupMessageToCache(qbChatMessage, qbUser.getId(), groupChatName);
    }

    private QBChatMessage getQBChatMessage(String body) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setBody(body);
        return qbChatMessage;
    }

    private void saveMessageToCache(QBChatMessage qbChatMessage, int senderId, int chatId) {
        DatabaseManager.savePrivateChatMessage(context, qbChatMessage, senderId, chatId);
    }

    private void saveGroupMessageToCache(QBChatMessage qbChatMessage, int senderId, String groupId){
        DatabaseManager.saveGroupChatMessage(context, qbChatMessage, senderId, groupId);
    }

    @Override
    public void processMessage(QBPrivateChat qbPrivateChat, QBChatMessage message) {
        saveMessageToCache(message, message.getSenderId(), message.getSenderId());
        Intent intent = new Intent(BroadcastActions.GOT_MESSAGE);
        intent.putExtra(BroadcastActions.EXTRA_MESSAGE, message.getBody());
        context.sendBroadcast(intent);
    }

    @Override
    public void chatCreated(QBPrivateChat qbPrivateChat, boolean createdLocally) {
        if (!createdLocally) {
            this.qbPrivateChat.addMessageListener(this);
        }
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

    public void initChats() {
        qbChatService = QBChatService.getInstance();
        qbPrivateChatManager = qbChatService.getPrivateChatManager();
        qbPrivateChatManager.addPrivateChatManagerListener(this);
        qbRoomChatManager = qbChatService.getRoomChatManager();
        qbRoomChatManager.addRoomChatManagerListener(this);
    }

    public void initPrivateChat(Context context, int opponentId) {
        this.context = context;
        qbUser = App.getInstance().getUser();
        qbPrivateChat = qbPrivateChatManager.createChat(opponentId, this);
        privateChatId = opponentId;
    }

    public void initRoomChat(Context context, String roomName, List<Friend> friends){
        this.context = context;
        qbUser = App.getInstance().getUser();
        qbRoomChat = qbRoomChatManager.createRoom(roomName);
        try{
            qbRoomChat.join();
            qbRoomChat.addRoomUser(qbUser.getId());
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