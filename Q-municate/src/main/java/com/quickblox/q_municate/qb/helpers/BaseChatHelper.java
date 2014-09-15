package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBChatState;
import com.quickblox.module.chat.QBChatStateListener;
import com.quickblox.module.chat.QBChatStateManager;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.ui.chats.FindUnknownFriendsTask;
import com.quickblox.q_municate.utils.ChatUtils;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ErrorUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseChatHelper extends BaseHelper {

    protected QBChatService chatService;
    protected QBUser chatCreator;
    protected QBPrivateChatManager privateChatManager;
    protected PrivateChatMessageListener privateChatMessageListener;
    protected QBChatStateManager chatStateManager;

    private QBPrivateChatManagerListener privateChatManagerListener;
    private List<QBNotificationChatListener> notificationChatListeners;

    public BaseChatHelper(Context context) {
        super(context);
        privateChatMessageListener = new PrivateChatMessageListener();
        privateChatManagerListener = new PrivateChatManagerListener();
        notificationChatListeners = new CopyOnWriteArrayList<QBNotificationChatListener>();
    }

    public void saveMessageToCache(MessageCache messageCache) {
        DatabaseManager.saveChatMessage(context, messageCache);
    }

    /*
    Call this method when you want start chating by existing dialog
     */
    public abstract QBChat createChatLocally(QBDialog dialogId, Bundle additional) throws QBResponseException;

    public abstract void closeChat(QBDialog dialogId, Bundle additional);

    public void init(QBChatService chatService, QBUser chatCreator) {
        this.chatService = chatService;
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);
        chatStateManager = chatService.getChatStateManager();
        chatStateManager.subscribeOnPrivateChat(privateChatManager);
        this.chatCreator = chatCreator;
    }

    protected void addNotificationChatListener(QBNotificationChatListener notificationChatListener) {
        notificationChatListeners.add(notificationChatListener);
    }

    protected String getMessageBody(QBChatMessage chatMessage) {
        String messageBody = chatMessage.getBody();
        if (TextUtils.isEmpty(messageBody)) {
            messageBody = Consts.EMPTY_STRING;
        }
        return messageBody;
    }

    protected void saveDialogToCache(Context context, QBDialog dialog) {
        DatabaseManager.saveDialog(context, dialog);
    }

    protected QBChatMessage getQBChatMessage(String body, QBFile file) {
        long time = DateUtils.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        if (file != null) {
            QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
            attachment.setUrl(file.getPublicUrl());
            chatMessage.addAttachment(attachment);
        }
        chatMessage.setProperty(ChatUtils.PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        chatMessage.setProperty(ChatUtils.PROPERTY_SAVE_TO_HISTORY, ChatUtils.VALUE_SAVE_TO_HISTORY);
        return chatMessage;
    }

    public void updateStatusMessage(QBDialog dialog, MessageCache messageCache) throws QBResponseException {
        updateStatusMessage(dialog.getDialogId(), messageCache.getId(), messageCache.isRead());
        sendMessageDeliveryStatus(messageCache.getPacketId(), messageCache.getId(),
                messageCache.getSenderId(), dialog.getType().getCode());
    }

    public void updateStatusMessage(String dialogId, String messageId,
            boolean isRead) throws QBResponseException {
        StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
        messagesIdsList.add(messageId);
        QBChatService.updateMessage(dialogId, messagesIdsList);
        DatabaseManager.updateStatusMessage(context, messageId, isRead);
    }

    public void updateMessageStatusDelivered(String messageId, boolean isDelivered) {
        DatabaseManager.updateMessageDeliveryStatus(context, messageId, isDelivered);
    }

    protected void sendPrivateMessage(QBChatMessage chatMessage, int opponentId,
            String dialogId) throws QBResponseException {
        QBPrivateChat privateChat = privateChatManager.getChat(opponentId);
        if (privateChat == null) {
            throw new QBResponseException("Private chat was not created!");
        }
        if (!TextUtils.isEmpty(dialogId)) {
            chatMessage.setProperty(ChatUtils.PROPERTY_DIALOG_ID, dialogId);
        }
        String error = null;
        try {
            privateChat.sendMessage(chatMessage);
        }catch (XMPPException e){
            error = context.getString(R.string.dlg_fail_connection);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }

    protected void sendMessageDeliveryStatus(String packedId, String messageId, int friendId,
            int dialogTypeCode) {
        QBPrivateChat chat = chatService.getPrivateChatManager().getChat(friendId);
        if (chat == null) {
            chat = chatService.getPrivateChatManager().createChat(friendId, null);
        }
        QBChatMessage chatMessage = ChatUtils.createNotificationMessageForDeliveryStatusRead(context,
                packedId, messageId, dialogTypeCode);
        try {
            chat.sendMessage(chatMessage);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    protected void notifyMessageReceived(QBChatMessage chatMessage, User user, String dialogId) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        String extraChatMessage;

        String fullName = user.getFullName();

        if (TextUtils.isEmpty(messageBody)) {
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }

        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, fullName);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected void updateDialogByNotification(QBChatMessage chatMessage) {
        long time = DateUtils.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessage(chatMessage, chatMessage.getBody(), time);
        if(dialog != null) {
            new FindUnknownFriendsTask(context).execute(null, dialog);
            saveDialogToCache(context, dialog);
        }
    }

    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {
    }

    public interface QBNotificationChatListener {

        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage);
    }

    private class PrivateChatManagerListener implements QBPrivateChatManagerListener {

        @Override
        public void chatCreated(QBPrivateChat privateChat, boolean b) {
            privateChat.addMessageListener(privateChatMessageListener);
        }
    }

    private class PrivateChatMessageListener implements QBChatStateListener<QBPrivateChat> {

        @Override
        public void processMessage(QBPrivateChat privateChat, QBChatMessage chatMessage) {
            if (ChatUtils.isNotificationMessage(chatMessage)) {
                for (QBNotificationChatListener notificationChatListener : notificationChatListeners) {
                    notificationChatListener.onReceivedNotification(chatMessage.getProperty(
                            ChatUtils.PROPERTY_NOTIFICATION_TYPE), chatMessage);
                }
            } else {
                onPrivateMessageReceived(privateChat, chatMessage);
            }
        }

        @Override
        public void stateChanged(QBPrivateChat privateChat, int chatParticipant, QBChatState chatState) {
            //TODO VF add composing state changed
        }
    }
}