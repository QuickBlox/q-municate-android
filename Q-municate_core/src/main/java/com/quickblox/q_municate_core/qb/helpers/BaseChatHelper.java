package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBIsTypingListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBGetTypingStatusCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.FindUnknownFriendsTask;
import com.quickblox.users.model.QBUser;

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
        DatabaseManager.saveChatMessage(context, messageCache, false);
    }

    /*
    Call this method when you want start chating by existing dialog
     */
    public abstract QBChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException;

    public abstract void closeChat(QBDialog dialogId, Bundle additional);

    public void init(QBUser chatCreator) {
        this.chatService = QBChatService.getInstance();
        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);
        this.chatCreator = chatCreator;
    }

    protected void addNotificationChatListener(QBNotificationChatListener notificationChatListener) {
        if (!notificationChatListeners.contains(notificationChatListener)) {
            notificationChatListeners.add(notificationChatListener);
        }
    }

    protected void saveDialogToCache(Context context, QBDialog dialog) {
        DatabaseManager.saveDialog(context, dialog);
    }

    protected QBChatMessage getQBChatMessage(String body, QBFile file) {
        long time = DateUtilsCore.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        if (file != null) {
            QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
            attachment.setUrl(file.getPublicUrl());
            chatMessage.addAttachment(attachment);
        }
        chatMessage.setProperty(ChatUtils.PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
        chatMessage.setProperty(ChatUtils.PROPERTY_SAVE_TO_HISTORY, ChatUtils.VALUE_SAVE_TO_HISTORY);
        return chatMessage;
    }

    public void updateStatusMessageRead(String dialogId, MessageCache messageCache, boolean forPrivate) throws Exception {
        updateStatusMessageReadServer(dialogId, messageCache, forPrivate);
        updateStatusMessageLocal(messageCache);
    }

    public void updateStatusMessageReadServer(String dialogId, MessageCache messageCache, boolean fromPrivate) throws Exception {
        StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
        messagesIdsList.add(messageCache.getId());
        QBChatService.markMessagesAsRead(dialogId, messagesIdsList);

        if (fromPrivate) {
            QBPrivateChat privateChat = privateChatManager.getChat(messageCache.getSenderId());
            privateChat.readMessage(messageCache.getId());
        }
    }

    public void updateStatusMessageLocal(MessageCache messageCache) throws QBResponseException {
        DatabaseManager.updateStatusMessage(context, messageCache);
    }

    public void updateMessageStatusDeliveredLocal(String messageId, boolean isDelivered) {
        DatabaseManager.updateMessageStatusDelivered(context, messageId, isDelivered);
    }

    public void sendIsTypingToServer(int opponentId) {
        QBPrivateChat privateChat = getPrivateChatByOpponent(opponentId);
        try {
            privateChat.sendIsTypingNotification();
        } catch (XMPPException e) {
            ErrorUtils.logError(e);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
        }
    }

    public void sendStopTypingToServer(int opponentId) {
        QBPrivateChat privateChat = getPrivateChatByOpponent(opponentId);
        try {
            privateChat.sendStopTypingNotification();
        } catch (XMPPException e) {
            ErrorUtils.logError(e);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
        }
    }

    private QBPrivateChat getPrivateChatByOpponent(int opponentId) {
        QBPrivateChat privateChat = privateChatManager.getChat(opponentId);
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(opponentId, privateChatMessageListener);
            privateChat.addIsTypingListener(privateChatIsTypingListener);
        }
        return privateChat;
    }

    public QBPrivateChat createChatIfNotExist(int opponentId) throws QBResponseException {
        QBDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(context, opponentId);
        return (QBPrivateChat) createChatLocally(existingPrivateDialog,
                ChatUtils.getBundleForCreatePrivateChat(opponentId));
    }

    protected void notifyMessageReceived(QBChatMessage chatMessage, User user, String dialogId,
            boolean isPrivateMessage) {
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
        long time = DateUtilsCore.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessageForUpdate(context, chatMessage, time);
        if (dialog != null) {
            new FindUnknownFriendsTask(context).execute(null, dialog);
            saveDialogToCache(context, dialog);
        }
    }

    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {
    }

    public interface QBNotificationChatListener {

        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage);
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
        public void processError(QBPrivateChat privateChat, QBChatException error,
                QBChatMessage originMessage) {
            // TODO: need to be implemented
        }

        @Override
        public void processMessageDelivered(QBPrivateChat privateChat, String messageId) {
            updateMessageStatusDeliveredLocal(messageId, true);
        }

        @Override
        public void processMessageRead(QBPrivateChat privateChat, String messageId) {
            try {
                MessageCache messageCache = new MessageCache();
                messageCache.setId(messageId);
                messageCache.setRead(true);
                updateStatusMessageLocal(messageCache);
            } catch (QBResponseException e) {
                ErrorUtils.logError(e);
            }
        }
    }

    private class PrivateChatManagerListener implements QBPrivateChatManagerListener {

        @Override
        public void chatCreated(QBPrivateChat privateChat, boolean b) {
            privateChat.addMessageListener(privateChatMessageListener);
            privateChat.addIsTypingListener(privateChatIsTypingListener);
        }
    }

    private class PrivateChatIsTypingListener implements QBIsTypingListener<QBPrivateChat> {

        @Override
        public void processUserIsTyping(QBPrivateChat qbPrivateChat) {
            QBGetTypingStatusCommand.start(context, true);
        }

        @Override
        public void processUserStopTyping(QBPrivateChat qbPrivateChat) {
            QBGetTypingStatusCommand.start(context, false);
        }
    }
}