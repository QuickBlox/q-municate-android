package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBGroupChat;
import com.quickblox.chat.QBGroupChatManager;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBIsTypingListener;
import com.quickblox.chat.listeners.QBMessageListener;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class QBBaseChatHelper extends BaseHelper {

    private static final String TAG = QBBaseChatHelper.class.getSimpleName();

    protected QBChatService chatService;
    protected QBUser chatCreator;
    protected QBDialog currentDialog;
    protected QBPrivateChatManager privateChatManager;
    protected PrivateChatMessageListener privateChatMessageListener;
    protected QBGroupChatManager groupChatManager;
    protected GroupChatMessageListener groupChatMessageListener;

    private PrivateChatIsTypingListener privateChatIsTypingListener;
    private PrivateChatManagerListener privateChatManagerListener;
    private List<QBNotificationChatListener> notificationChatListeners;

    public QBBaseChatHelper(Context context) {
        super(context);
        privateChatMessageListener = new PrivateChatMessageListener();
        privateChatManagerListener = new PrivateChatManagerListener();
        privateChatIsTypingListener = new PrivateChatIsTypingListener();

        groupChatMessageListener = new GroupChatMessageListener();

        notificationChatListeners = new CopyOnWriteArrayList<QBNotificationChatListener>();
    }

    public void saveMessageToCache(MessageCache messageCache) {
        ChatDatabaseManager.saveChatMessageGlobal(context, messageCache);
    }

    /*
    Call this method when you want start chating by existing dialog
     */
    public abstract QBChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException;

    public abstract void closeChat(QBDialog dialogId, Bundle additional);

    public void init(QBUser chatCreator) {
        this.chatService = QBChatService.getInstance();
        this.chatCreator = chatCreator;

        privateChatManager = chatService.getPrivateChatManager();
        privateChatManager.addPrivateChatManagerListener(privateChatManagerListener);

        groupChatManager = chatService.getGroupChatManager();
    }

    protected void addNotificationChatListener(QBNotificationChatListener notificationChatListener) {
        notificationChatListeners.add(notificationChatListener);
    }

    public void sendPrivateMessage(QBChatMessage chatMessage, int opponentId,
            String dialogId) throws QBResponseException {
        addNecessaryPropertyForQBChatMessage(chatMessage, dialogId);

        sendPrivateMessage(chatMessage, opponentId);
        savePrivateMessageToCache(chatMessage, dialogId, opponentId);
    }

    public void sendPrivateMessage(QBChatMessage chatMessage, int opponentId) throws QBResponseException {
        QBPrivateChat privateChat = createPrivateChatIfNotExist(opponentId);

        chatMessage.setMarkable(true);

        String error = null;
        try {
            privateChat.sendMessage(chatMessage);
        } catch (XMPPException e) {
            error = context.getString(R.string.dlg_fail_connection);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }

    protected void addNecessaryPropertyForQBChatMessage(QBChatMessage chatMessage, String dialogId) {
        long time = DateUtilsCore.getCurrentTime();
        chatMessage.setProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID, dialogId);
        chatMessage.setProperty(ChatNotificationUtils.PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
    }

    private void savePrivateMessageToCache(QBChatMessage chatMessage, String dialogId, int opponentId) {
        String messageId = chatMessage.getId();
        long time = Long.parseLong(chatMessage.getProperty(ChatNotificationUtils.PROPERTY_DATE_SENT)
                .toString());
        String attachUrl = ChatUtils.getAttachUrlFromMessage(chatMessage.getAttachments());
        Integer recipientId;

        if (chatMessage.getRecipientId() == null) {
            recipientId = opponentId;
        } else {
            recipientId = chatMessage.getRecipientId();
        }

        MessageCache messageCache = new MessageCache(messageId, dialogId, chatCreator.getId(), recipientId,
                chatMessage.getBody(), attachUrl, time, false, false, true);

        if (ChatNotificationUtils.isNotificationMessage(chatMessage)) {
            int friendsMessageTypeCode = ChatNotificationUtils.getNotificationTypeIfExist(chatMessage);
            messageCache.setMessagesNotificationType(MessagesNotificationType.parseByCode(
                    friendsMessageTypeCode));
        }

        saveMessageToCache(messageCache);
    }

    protected void saveDialogToCache(QBDialog dialog) {
        ChatDatabaseManager.saveDialog(context, dialog);
    }

    protected void saveDialogsToCache(List<QBDialog> chatDialogsList) {
        ChatDatabaseManager.saveDialogs(context, chatDialogsList);
    }

    public List<QBDialog> getDialogs() throws QBResponseException, XMPPException, SmackException {
        Bundle bundle = new Bundle();
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        List<QBDialog> chatDialogsList = QBChatService.getChatDialogs(null, customObjectRequestBuilder,
                bundle);

        if (chatDialogsList != null && !chatDialogsList.isEmpty()) {
            saveDialogsToCache(chatDialogsList);
        }

        return chatDialogsList;
    }

    public List<QBChatMessage> getDialogMessages(QBRequestGetBuilder customObjectRequestBuilder,
                                                        Bundle returnedBundle, QBDialog dialog,
                                                        long lastDateLoad) throws QBResponseException {

        if (lastDateLoad != ConstsCore.ZERO_LONG_VALUE) {
            customObjectRequestBuilder.gt(com.quickblox.chat.Consts.MESSAGE_DATE_SENT, lastDateLoad);
        } else {
            deleteMessagesByDialogId(dialog.getDialogId());
        }

        List<QBChatMessage> dialogMessagesList = QBChatService.getDialogMessages(dialog,
                customObjectRequestBuilder, returnedBundle);

        if (dialogMessagesList != null) {
            ChatDatabaseManager.saveChatMessages(context, dialogMessagesList, dialog.getDialogId());
        }

        return dialogMessagesList;
    }

    private void deleteMessagesByDialogId(String dialogId) {
        ChatDatabaseManager.deleteMessagesByDialogId(context, dialogId);
    }

    private void deleteDialogLocal(String dialogId) {
        ChatDatabaseManager.deleteDialogByDialogId(context, dialogId);
    }

    public void deleteDialog(String dialogId, QBDialogType dialogType) {
        try {
            if (QBDialogType.PRIVATE.equals(dialogType)) {
                QBChatService.getInstance().getPrivateChatManager().deleteDialog(dialogId);
            } else {
                QBChatService.getInstance().getGroupChatManager().deleteDialog(dialogId);
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        deleteDialogLocal(dialogId);
    }

    protected QBChatMessage getQBChatMessage(String body, QBFile file) {
        long time = DateUtilsCore.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);

        if (file != null) {
            QBAttachment attachment = getAttachment(file);
            chatMessage.addAttachment(attachment);
        }

        chatMessage.setProperty(ChatNotificationUtils.PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
        chatMessage.setProperty(ChatNotificationUtils.PROPERTY_SAVE_TO_HISTORY,
                ChatNotificationUtils.VALUE_SAVE_TO_HISTORY);

        return chatMessage;
    }

    private QBAttachment getAttachment(QBFile file) {
        // TODO temp value
        String contentType = "image/jpeg";

        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
        attachment.setId(file.getUid());
        attachment.setName(file.getName());
        attachment.setContentType(contentType);
        attachment.setUrl(file.getPublicUrl());
        attachment.setSize(file.getSize());

        return attachment;
    }

    public void sendTypingStatusToServer(int opponentId, boolean startTyping) {
        QBPrivateChat privateChat = createPrivateChatIfNotExist(opponentId);

        try {
            if (startTyping) {
                privateChat.sendIsTypingNotification();
            } else {
                privateChat.sendStopTypingNotification();
            }
        } catch (XMPPException e) {
            ErrorUtils.logError(e);
        } catch (SmackException.NotConnectedException e) {
            ErrorUtils.logError(e);
        }
    }

    public QBPrivateChat createPrivateChatIfNotExist(int userId) {
        QBPrivateChat privateChat = privateChatManager.getChat(userId);
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(userId, null);
        }
        return privateChat;
    }

    public QBDialog createPrivateChatOnRest(int opponentId) throws QBResponseException {
        QBDialog dialog = privateChatManager.createDialog(opponentId);
        return dialog;
    }

    public QBDialog createPrivateDialogIfNotExist(int userId, String lastMessage) throws QBResponseException {
        QBDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(context, userId);
        if (existingPrivateDialog == null) {
            existingPrivateDialog = createPrivateChatOnRest(userId);
            saveDialogToCache(existingPrivateDialog);
        }
        existingPrivateDialog.setLastMessage(lastMessage);
        return existingPrivateDialog;
    }

    public QBDialog createPrivateDialogIfNotExist(int userId) throws QBResponseException {
        QBDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(context, userId);
        if (existingPrivateDialog == null) {
            existingPrivateDialog = createPrivateChatOnRest(userId);
            saveDialogToCache(existingPrivateDialog);
        }
        return existingPrivateDialog;
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

    protected void notifyMessageTyping(boolean isTyping) {
        Intent intent = new Intent(QBServiceConsts.TYPING_MESSAGE);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TYPING, isTyping);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected MessageCache parseReceivedMessage(QBChatMessage chatMessage) {
        long time;
        String attachUrl;

        time = Long.parseLong(chatMessage.getProperty(ChatNotificationUtils.PROPERTY_DATE_SENT));
        attachUrl = ChatUtils.getAttachUrlIfExists(chatMessage);
        String dialogId = chatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        Integer recipientId;

        if (chatMessage.getRecipientId() == null) {
            recipientId = ConstsCore.ZERO_INT_VALUE;
        } else {
            recipientId = chatMessage.getRecipientId();
        }

        MessageCache messageCache = new MessageCache(chatMessage.getId(), dialogId, chatMessage.getSenderId(),
                recipientId, chatMessage.getBody(), attachUrl, time, false, false, false);

        return messageCache;
    }

    public void updateStatusMessageRead(String dialogId, MessageCache messageCache,
            boolean forPrivate) throws Exception {
        updateStatusMessageReadServer(dialogId, messageCache, forPrivate);
        updateStatusMessageLocal(messageCache);
    }

    public void updateStatusMessageReadServer(String dialogId, MessageCache messageCache,
            boolean fromPrivate) throws Exception {
        StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
        messagesIdsList.add(messageCache.getId());
        QBChatService.markMessagesAsRead(dialogId, messagesIdsList);

        if (fromPrivate) {
            QBPrivateChat privateChat = createPrivateChatIfNotExist(messageCache.getSenderId());
            privateChat.readMessage(messageCache.getId());
        }
    }

    public void updateStatusMessageLocal(MessageCache messageCache) throws QBResponseException {
        ChatDatabaseManager.updateStatusMessage(context, messageCache);
    }

    public void updateMessageStatusDeliveredLocal(String messageId, boolean isDelivered) {
        ChatDatabaseManager.updateMessageStatusDelivered(context, messageId, isDelivered);
    }

    public void onGroupMessageReceived(QBChat groupChat, final QBChatMessage chatMessage) {
    }

    public void onPrivateMessageReceived(QBChat privateChat, final QBChatMessage chatMessage) {
    }

    public interface QBNotificationChatListener {

        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage);
    }

    private class GroupChatMessageListener implements QBMessageListener<QBGroupChat> {

        @Override
        public void processMessage(QBGroupChat groupChat, QBChatMessage chatMessage) {
            onGroupMessageReceived(groupChat, chatMessage);
        }

        @Override
        public void processError(QBGroupChat groupChat, QBChatException error, QBChatMessage originMessage) {

        }

        @Override
        public void processMessageDelivered(QBGroupChat groupChat, String messageID) {
            // never called
        }

        @Override
        public void processMessageRead(QBGroupChat groupChat, String messageID) {
            // never called
        }
    }

    private class PrivateChatMessageListener implements QBMessageListener<QBPrivateChat> {

        @Override
        public void processMessage(QBPrivateChat privateChat, final QBChatMessage chatMessage) {
            if (ChatNotificationUtils.isNotificationMessage(chatMessage)) {
                for (QBNotificationChatListener notificationChatListener : notificationChatListeners) {
                    notificationChatListener.onReceivedNotification(chatMessage.getProperty(
                            ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE), chatMessage);
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
        public void processUserIsTyping(QBPrivateChat privateChat) {
            notifyMessageTyping(true);
        }

        @Override
        public void processUserStopTyping(QBPrivateChat privateChat) {
            notifyMessageTyping(false);
        }
    }
}