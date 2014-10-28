package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.exception.QBChatException;
import com.quickblox.module.chat.listeners.QBIsTypingListener;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBChatMessage;
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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseChatHelper extends BaseHelper {

    protected QBChatService chatService;
    protected QBUser chatCreator;
    protected QBPrivateChatManager privateChatManager;
    protected PrivateChatMessageListener privateChatMessageListener;
    protected PrivateChatIsTypingListener privateChatIsTypingListener;

    private QBPrivateChatManagerListener privateChatManagerListener;
    private List<QBNotificationChatListener> notificationChatListeners;

    public BaseChatHelper(Context context) {
        super(context);
        privateChatMessageListener = new PrivateChatMessageListener();
        privateChatManagerListener = new PrivateChatManagerListener();
        privateChatIsTypingListener = new PrivateChatIsTypingListener();
        notificationChatListeners = new CopyOnWriteArrayList<QBNotificationChatListener>();
    }

    public void saveMessageToCache(MessageCache messageCache) {
        DatabaseManager.saveChatMessage(context, messageCache);
    }

    /*
    Call this method when you want start chating by existing dialog
     */
    public abstract QBChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException;

    public abstract void closeChat(QBDialog dialogId, Bundle additional);

    public void init(QBChatService chatService, QBUser chatCreator) {
        this.chatService = chatService;
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);
        this.chatCreator = chatCreator;
    }

    protected void addNotificationChatListener(QBNotificationChatListener notificationChatListener) {
        notificationChatListeners.add(notificationChatListener);
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

    public void updateStatusMessage(QBDialog dialog, MessageCache messageCache) throws Exception {
        updateStatusMessage(dialog.getDialogId(), messageCache.getId(), messageCache.isRead());
        sendMessageDeliveryStatus(messageCache.getPacketId(), messageCache.getId(),
                messageCache.getSenderId(), dialog.getType().getCode());
    }

    public void updateStatusMessage(String dialogId, String messageId,
            boolean isRead) throws QBResponseException {
        StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
        messagesIdsList.add(messageId);

        QBChatService.markMessagesAsRead(dialogId, messagesIdsList);

        DatabaseManager.updateStatusMessage(context, messageId, isRead);
    }

    public void updateMessageStatusDelivered(String messageId, boolean isDelivered) {
        DatabaseManager.updateMessageDeliveryStatus(context, messageId, isDelivered);
    }

    public QBPrivateChat createChatIfNotExist(int opponentId) throws QBResponseException {
        QBDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(context, opponentId);
        return  (QBPrivateChat) createChatLocally(existingPrivateDialog, ChatUtils.getBundleForCreatePrivateChat(opponentId));
    }

    protected void sendMessageDeliveryStatus(String packedId, String messageId, int friendId,
            int dialogTypeCode) throws XMPPException, SmackException.NotConnectedException {
        QBPrivateChat chat = chatService.getPrivateChatManager().getChat(friendId);
        if (chat == null) {
            chat = chatService.getPrivateChatManager().createChat(friendId, null);
        }
        QBChatMessage chatMessage = ChatUtils.createNotificationMessageForDeliveryStatusRead(context,
                packedId, messageId, dialogTypeCode);
        chat.sendMessage(chatMessage);
    }

    protected void notifyMessageReceived(QBChatMessage chatMessage, User user, String dialogId, boolean isPrivateMessage) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = chatMessage.getBody();
        String extraChatMessage;

        if (!chatMessage.getAttachments().isEmpty()) {
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }

        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE, isPrivateMessage);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected void updateDialogByNotification(QBChatMessage chatMessage) {
        long time = DateUtils.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessageForUpdate(context, chatMessage, time);
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
            privateChat.addIsTypingListener(privateChatIsTypingListener);
        }
    }

    private class PrivateChatMessageListener implements QBMessageListener<QBPrivateChat> {
        @Override
        public void processMessage(QBPrivateChat privateChat, final QBChatMessage chatMessage) {
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
        public void processError(QBPrivateChat privateChat, QBChatException error, QBChatMessage originMessage){

        }

        @Override
        public void processMessageDelivered(QBPrivateChat privateChat, String messageID){

        }

        @Override
        public void processMessageRead(QBPrivateChat privateChat, String messageID){

        }
    }

    private class PrivateChatIsTypingListener implements QBIsTypingListener<QBPrivateChat>{
        @Override
        public void processUserIsTyping(QBPrivateChat qbPrivateChat) {

        }

        @Override
        public void processUserStopTyping(QBPrivateChat qbPrivateChat) {

        }
    }
}