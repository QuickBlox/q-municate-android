package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.content.QBContent;
import com.quickblox.module.content.model.QBFile;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.DialogMessageCache;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;
import com.quickblox.qmunicate.utils.ErrorUtils;

import java.io.File;
import java.util.List;

public class QBPrivateChatHelper extends BaseChatHelper implements QBPrivateChatManagerListener {

    private QBPrivateChatManager privateChatManager;
    private QBPrivateChat privateChat;
    private int opponentId;
    private QBNotificationChatListener notificationChatListener = new PrivateChatNotificationListener();
    private QBDialog currentDialog;

    public QBPrivateChatHelper(Context context) {
        super(context);
    }

    public void init(QBChatService chatService, QBUser user) {
        super.init(chatService, user);
        privateChatManager = chatService.getPrivateChatManager();
        addNotificationChatListener(notificationChatListener);
    }

    public void sendPrivateMessage(String message, int userId) throws QBResponseException {
        sendPrivateMessage(null, message, userId);
    }

    public void sendPrivateMessageWithAttachImage(QBFile file, int userId) throws QBResponseException {
        sendPrivateMessage(file, null, userId);
    }

    //TODO add dialodId to save in cache
    private void sendPrivateMessage(QBFile file, String message, int userId) throws QBResponseException {
        QBChatMessage chatMessage;
        if (file != null) {
            chatMessage = getQBChatMessageWithImage(file);
        } else {
            chatMessage = getQBChatMessage(message);
        }
        String dialogId = null;
        if (currentDialog != null) {
            dialogId = currentDialog.getDialogId();
        }
        sendPrivateMessage(chatMessage, userId, dialogId);
        String attachUrl = file != null ? file.getPublicUrl() : Consts.EMPTY_STRING;
        long time = Long.parseLong(chatMessage.getProperty(PROPERTY_DATE_SENT).toString());
        if (dialogId != null) {
            saveMessageToCache(new DialogMessageCache(dialogId, userId, chatMessage.getBody(), attachUrl,
                    time, true));
        }
    }

    @Override
    public QBPrivateChat createChatLocally(QBDialog currentDialog, Bundle additional) {
        int opponentId = additional.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        QBPrivateChat privateChat = createChat(opponentId);
        this.currentDialog = currentDialog;
        return privateChat;
    }

    @Override
    public void closeChat(QBDialog dialogId, Bundle additional) {
        currentDialog = null;
    }

    private QBPrivateChat createChat(int opponentId) {
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
        this.opponentId = opponentId;
        return dialog;
    }

    public void closeChat(int opponentId) {
        currentDialog = null;
    }

    private void notifyFriendCreatedPrivateChat(QBDialog dialog, int opponentId) throws QBResponseException {
        long time = DateUtils.getCurrentTime();
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentId);
        QBPrivateChat chat = createChat(opponentId);
        QBChatMessage chatMessage = ChatUtils.createChatNotificationMessage(context, dialog);
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
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

    private void saveDialogsToCache(List<QBDialog> dialogsList) {
        DatabaseManager.saveDialogs(context, dialogsList);
    }

    private void saveChatMessagesToCache(List<QBHistoryMessage> dialogMessagesList, String dialogId) {
        DatabaseManager.saveChatMessages(context, dialogMessagesList, dialogId);
    }

    public void updateStatusMessage(String messageId, boolean isRead) {
        DatabaseManager.updateStatusMessage(context, messageId, isRead);
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

    //TODO will be defined when update dialog signaling will be integrated
    private void updateDialogByNotification(QBChatMessage chatMessage) {

    }

    @Override
    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        Friend friend = DatabaseManager.getFriendById(context, chatMessage.getSenderId());
        if (friend == null) {
            friend = new Friend();
            friend.setFullname(Consts.EMPTY_STRING + chatMessage.getSenderId());
        }
        long time;
        String roomJidId;
        String attachUrl = null;

        time = Long.parseLong(chatMessage.getProperty(PROPERTY_DATE_SENT).toString());
        attachUrl = ChatUtils.getAttachUrlIfExists(chatMessage);
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        saveMessageToCache(new DialogMessageCache(dialogId, chatMessage.getSenderId(), chatMessage.getBody(),
                attachUrl, time, false));
        notifyMessageReceived(chatMessage, friend, dialogId);
    }

    private class PrivateChatNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT.equals(notificationType)) {
                createDialogByNotification(chatMessage);
            } else {
                updateDialogByNotification(chatMessage);
            }
        }
    }
}