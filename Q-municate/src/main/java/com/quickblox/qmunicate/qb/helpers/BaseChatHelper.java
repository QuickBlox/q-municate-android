package com.quickblox.qmunicate.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.helper.StringifyArrayList;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.chat.QBChat;
import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBChatService;
import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.QBPrivateChat;
import com.quickblox.module.chat.QBPrivateChatManager;
import com.quickblox.module.chat.listeners.QBMessageListener;
import com.quickblox.module.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
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

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class BaseChatHelper extends BaseHelper {

    protected QBChatService chatService;
    protected QBUser chatCreator;
    protected static String PROPERTY_MESSAGE_ID = "chat_message_id";
    protected static String PROPERTY_DATE_SENT = "date_sent";
    protected static String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    protected static String VALUE_SAVE_TO_HISTORY = "1";
    protected QBPrivateChatManager privateChatManager;
    protected PrivateChatMessageListener privateChatMessageListener = new PrivateChatMessageListener();

    private QBPrivateChatManagerListener privateChatManagerListener = new PrivateChatManagerListener();
    private List<QBNotificationChatListener> notificationChatListeners = new CopyOnWriteArrayList<QBNotificationChatListener>();

    public interface QBNotificationChatListener {

        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage);
    }

    public BaseChatHelper(Context context) {
        super(context);
    }

    public void saveMessageToCache(DialogMessageCache dialogMessageCache) {
        DatabaseManager.saveChatMessage(context, dialogMessageCache);
    }

    /*
    Call this method when you want start chating by existing dialog
     */
    public abstract QBChat createChatLocally(QBDialog dialogId, Bundle additional);

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

    protected QBChatMessage getQBChatMessage(String body) {
        long time = DateUtils.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        return chatMessage;
    }

    protected QBChatMessage getQBChatMessageWithImage(QBFile qbFile) {
        long time = DateUtils.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        QBAttachment attachment = new QBAttachment(QBAttachment.PHOTO_TYPE);
        attachment.setUrl(qbFile.getPublicUrl());
        chatMessage.addAttachment(attachment);
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        return chatMessage;
    }

    public void updateStatusMessage(QBDialog dialog, String messageId, long dateSent, boolean isRead) throws QBResponseException {
        StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
        messagesIdsList.add(messageId);

        // TODO Sergey Fedunets: temp decision
        if(QBDialogType.GROUP.equals(dialog.getType())) {
            Bundle bundle = new Bundle();
            QBCustomObjectRequestBuilder customObjectRequestBuilder = new QBCustomObjectRequestBuilder();
            customObjectRequestBuilder.eq(com.quickblox.internal.module.chat.Consts.MESSAGE_DATE_SENT, dateSent);
            QBChatService.getDialogMessages(dialog, customObjectRequestBuilder, bundle);
        }
        // end of todo
        else {
            QBChatService.updateMessage(dialog.getDialogId(), messagesIdsList);
        }

        DatabaseManager.updateStatusMessage(context, messageId, isRead);
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
        } catch (XMPPException e) {
            error = context.getString(R.string.dlg_fail_send_msg);
        } catch (SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }

    protected void notifyMessageReceived(QBChatMessage chatMessage, Friend friend, String dialogId) {
        Intent intent = new Intent(QBServiceConsts.GOT_CHAT_MESSAGE);
        String messageBody = getMessageBody(chatMessage);
        String extraChatMessage;
        String fullname = friend.getFullname();
        if (TextUtils.isEmpty(messageBody)) {
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }

        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_SENDER_CHAT_MESSAGE, fullname);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {

    }

    private class PrivateChatManagerListener implements QBPrivateChatManagerListener {

        @Override
        public void chatCreated(QBPrivateChat qbPrivateChat, boolean b) {
            qbPrivateChat.addMessageListener(privateChatMessageListener);
        }
    }

    private class PrivateChatMessageListener implements QBMessageListener<QBPrivateChat> {

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
    }
}
