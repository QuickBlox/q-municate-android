package com.quickblox.q_municate.qb.helpers;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.FriendsNotificationType;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.ChatUtils;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.ErrorUtils;
import com.quickblox.q_municate.utils.Utils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;

public class QBPrivateChatHelper extends BaseChatHelper implements QBPrivateChatManagerListener {

    private static final String TAG = QBPrivateChatHelper.class.getSimpleName();
    private QBPrivateChatManager privateChatManager;
    private QBNotificationChatListener notificationChatListener;
    private QBDialog currentDialog;

    public QBPrivateChatHelper(Context context) {
        super(context);
    }

    public void sendPrivateMessage(String message, int userId) throws QBResponseException {
        sendPrivateMessage(null, message, userId);
    }

    public void sendPrivateMessageWithAttachImage(QBFile file, int userId) throws QBResponseException {
        sendPrivateMessage(file, context.getString(R.string.dlg_attached_last_message), userId);
    }

    public void sendPrivateMessage(QBChatMessage chatMessage, int opponentId,
            String dialogId) throws QBResponseException {
        QBPrivateChat privateChat = privateChatManager.getChat(opponentId);
        chatMessage.setMarkable(true);
        if (privateChat == null) {
            privateChat = createChatIfNotExist(opponentId);
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
        String messageId = chatMessage.getId();
        if (dialogId != null) {
            saveMessageToCache(new MessageCache(messageId, dialogId, chatCreator.getId(),
                    chatMessage.getBody(), attachUrl, time, false, false, true));
        }
    }

    @Override
    public QBPrivateChat createChatLocally(QBDialog dialog,
            Bundle additional) throws QBResponseException {
        int opponentId = additional.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        QBPrivateChat privateChat = createChat(opponentId);
        this.currentDialog = dialog;
        return privateChat;
    }

    @Override
    public void closeChat(QBDialog dialogId, Bundle additional) {
        currentDialog = null;
    }

    public void init(QBUser user) {
        super.init(user);
        privateChatManager = chatService.getPrivateChatManager();
        notificationChatListener = new PrivateChatNotificationListener();
        addNotificationChatListener(notificationChatListener);
    }

    @Override
    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        User user = DatabaseManager.getUserById(context, chatMessage.getSenderId());

        if (user == null) {
            user = new User();
            user.setFullName(Consts.EMPTY_STRING + chatMessage.getSenderId());
        }

        MessageCache messageCache = parseReceivedMessage(chatMessage);

        saveMessageToCache(messageCache);
        notifyMessageReceived(chatMessage, user, messageCache.getDialogId(), true);
    }

    private MessageCache parseReceivedMessage(QBChatMessage chatMessage) {
        String messageId;
        long time;
        String attachUrl;

        messageId = chatMessage.getProperty(ChatUtils.PROPERTY_MESSAGE_ID);
        time = Long.parseLong(chatMessage.getProperty(ChatUtils.PROPERTY_DATE_SENT));
        attachUrl = ChatUtils.getAttachUrlIfExists(chatMessage);
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        MessageCache messageCache = new MessageCache(messageId, dialogId, chatMessage.getSenderId(),
                chatMessage.getBody(), attachUrl, time, false, false, false);

        return messageCache;
    }

    private QBPrivateChat createChat(int opponentId) throws QBResponseException {
        boolean notNull = Utils.validateNotNull(privateChatManager);
        if (!notNull) {
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
        privateChat.addIsTypingListener(privateChatIsTypingListener);
    }

    public void updateDialog(QBDialog dialog) {
        int countUnreadDialog = DatabaseManager.getCountUnreadMessagesByDialogIdLocal(context, dialog.getDialogId());
        DatabaseManager.updateDialog(context, dialog.getDialogId(), dialog.getLastMessage(),
                dialog.getLastMessageDateSent(), dialog.getLastMessageUserId(), countUnreadDialog);
    }

    public QBDialog createPrivateChatOnRest(int opponentId) throws QBResponseException {
        QBDialog dialog = privateChatManager.createDialog(opponentId);
        saveDialogToCache(context, dialog);
        notifyFriendCreatedPrivateChat(dialog, opponentId);
        return dialog;
    }

    private void notifyFriendCreatedPrivateChat(QBDialog dialog, int opponentId) throws QBResponseException {
        long time = DateUtils.getCurrentTime();
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentId);
        QBPrivateChat chat = createChat(opponentId);
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
        time = DateUtils.getCurrentTime();
        QBDialog dialog = ChatUtils.parseDialogFromMessage(context, chatMessage, chatMessage.getBody(), time);
        if (QBDialogType.PRIVATE.equals(dialog.getType())) {
            saveDialogToCache(context, dialog);
        }
    }

    private class PrivateChatNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT.equals(notificationType)) {
                createDialogByNotification(chatMessage);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, FriendsNotificationType.REQUEST);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_ACCEPT_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, FriendsNotificationType.ACCEPT);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REJECT_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, FriendsNotificationType.REJECT);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REMOVE_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, FriendsNotificationType.REMOVE);
            } else {
                updateDialogByNotification(chatMessage);
            }
        }
    }

    private void friendRequestMessageReceived(QBChatMessage chatMessage, FriendsNotificationType type) {
        MessageCache messageCache = parseReceivedMessage(chatMessage);
        messageCache.setFriendsNotificationType(type);
        saveMessageToCache(messageCache);
    }
}