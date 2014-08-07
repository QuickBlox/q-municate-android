package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.caching.DatabaseManager;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.model.Friend;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.ChatUtils;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.DialogUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.Utils;

import java.io.File;

public class QBPrivateChatHelper extends BaseChatHelper implements QBPrivateChatManagerListener {

    private static final String TAG = QBPrivateChatHelper.class.getSimpleName();
    private QBPrivateChatManager privateChatManager;
    private QBNotificationChatListener notificationChatListener;
    private QBDialog currentDialog;

    public QBPrivateChatHelper(Context context) {
        super(context);
    }

    public void init(QBChatService chatService, QBUser user) {
        super.init(chatService, user);
        privateChatManager = chatService.getPrivateChatManager();
        notificationChatListener = new PrivateChatNotificationListener();
        addNotificationChatListener(notificationChatListener);
    }

    public void sendPrivateMessage(String message, int userId) throws QBResponseException {
        sendPrivateMessage(null, message, userId);
    }

    public void sendPrivateMessageWithAttachImage(QBFile file, int userId) throws QBResponseException {
        sendPrivateMessage(file, context.getString(
                R.string.dlg_attached_last_message), userId);
    }

    private void sendPrivateMessage(QBFile file, String message, int userId) throws QBResponseException {
        QBChatMessage chatMessage;
        chatMessage = getQBChatMessage(message, file);
        String dialogId = null;
        if (currentDialog != null) {
            dialogId = currentDialog.getDialogId();
        }
        sendPrivateMessage(chatMessage, userId, dialogId);
        String attachUrl = file != null ? file.getPublicUrl() : Consts.EMPTY_STRING;
        long time = Long.parseLong(chatMessage.getProperty(ChatUtils.PROPERTY_DATE_SENT).toString());
        String messageId = chatMessage.getPacketId();
        String packetId = chatMessage.getPacketId();
        DialogUtils.showLong(context, "sendPrivateMessage(), packedId = " + messageId);
        if (dialogId != null) {
            saveMessageToCache(new MessageCache(messageId, dialogId, packetId, chatCreator.getId(), chatMessage.getBody(),
                    attachUrl, time, true, false));
        }
    }

    @Override
    public QBPrivateChat createChatLocally(QBDialog currentDialog, Bundle additional) throws QBResponseException {
        int opponentId = additional.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        QBPrivateChat privateChat = createChat(opponentId);
        this.currentDialog = currentDialog;
        return privateChat;
    }

    @Override
    public void closeChat(QBDialog dialogId, Bundle additional) {
        currentDialog = null;
    }

    private QBPrivateChat createChat(int opponentId) throws QBResponseException {
        boolean notNull = Utils.validateNotNull(privateChatManager);
        if( !notNull){
            ErrorUtils.logError(TAG, " privateChatManager is NULL");
            throw new QBResponseException(context.getString(R.string.dlg_fail_create_chat));
        }
        QBPrivateChat privateChat = privateChatManager.getChat(opponentId);
        if (privateChat == null) {
            privateChat = privateChatManager.createChat(opponentId, privateChatMessageListener);
        }
        return privateChat;
    }

    private void sendPrivateMessage(QBChatMessage chatMessage, int opponentId) throws QBResponseException {
        sendPrivateMessage(chatMessage, opponentId, Consts.EMPTY_STRING);
    }

    @Override
    public void chatCreated(QBPrivateChat privateChat, boolean createdLocally) {
        privateChat.addMessageListener(privateChatMessageListener);
    }

    public void updateDialog(QBDialog dialog) {
        DatabaseManager.updateDialog(context, dialog.getDialogId(), dialog.getLastMessage(),
                dialog.getLastMessageDateSent(), dialog.getLastMessageUserId());
    }

    public QBDialog createPrivateChatOnRest(int opponentId) throws QBResponseException {
        QBDialog dialog = privateChatManager.createDialog(opponentId);
        saveDialogToCache(context, dialog);
        try {
            notifyFriendCreatedPrivateChat(dialog, opponentId);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
        return dialog;
    }

    public void closeChat(int opponentId) {
        currentDialog = null;
    }

    private void notifyFriendCreatedPrivateChat(QBDialog dialog, int opponentId) throws QBResponseException {
        long time = DateUtils.getCurrentTime();
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentId);
        QBChatMessage chatMessage = ChatUtils.createChatNotificationMessage(context, dialog);
        chatMessage.setProperty(ChatUtils.PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        sendPrivateMessage(chatMessage, opponentId);
    }

    public QBFile loadAttachFile(File inputFile) throws Exception {
        QBFile file = null;
        try {
            file = QBContent.uploadFileTask(inputFile, true, (String) null);
        } catch (QBResponseException exc) {
            throw new Exception(context.getString(R.string.dlg_fail_upload_attach));
        }

        return file;
    }

    private void createDialogByNotification(QBChatMessage chatMessage) {
        long time;
        String attachUrl = null;
        time = DateUtils.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessage(chatMessage, chatMessage.getBody(), time);
        if (QBDialogType.PRIVATE.equals(dialog.getType())) {
            saveDialogToCache(context, dialog);
        }
    }

    private void messageDeliveryStatusReceived(QBChatMessage chatMessage) {
        String messageId;
        String dialogTypeCodeString = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_TYPE_CODE);
        boolean messageDelivered = true;

        if(QBDialogType.PRIVATE.equals(ChatUtils.parseDialogType(dialogTypeCodeString))) {
            messageId = chatMessage.getPacketId();
        } else {
            messageId = chatMessage.getProperty(ChatUtils.PROPERTY_MESSAGE_ID);
        }

        updateMessageStatusDelivered(messageId, messageDelivered);
    }

    @Override
    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        Log.d("debug_statuses", "onPrivateMessageReceived(), chatMessage.getPacketId() = " + chatMessage.getPacketId());
        Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
        if (friend == null) {
            friend = new Friend();
            friend.setFullname(Consts.EMPTY_STRING + chatMessage.getSenderId());
        }

        String messageId;
        long time;
        String attachUrl;

        messageId = chatMessage.getProperty(ChatUtils.PROPERTY_MESSAGE_ID);
        time = Long.parseLong(chatMessage.getProperty(ChatUtils.PROPERTY_DATE_SENT));
        attachUrl = ChatUtils.getAttachUrlIfExists(chatMessage);
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        String packetId = chatMessage.getPacketId();
        saveMessageToCache(new MessageCache(messageId, dialogId, packetId, chatMessage.getSenderId(), chatMessage.getBody(),
                attachUrl, time, false, false));
        notifyMessageReceived(chatMessage, friend, dialogId);
    }

    private class PrivateChatNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            Log.d("debug_statuses", "onReceivedNotification(), chatMessage.getPacketId() = " + chatMessage.getPacketId());
            if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT.equals(notificationType)) {
                createDialogByNotification(chatMessage);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_MESSAGE_DELIVERY_STATUS.equals(notificationType)) {
                messageDeliveryStatusReceived(chatMessage);
            } else {
                updateDialogByNotification(chatMessage);
            }
        }
    }
}