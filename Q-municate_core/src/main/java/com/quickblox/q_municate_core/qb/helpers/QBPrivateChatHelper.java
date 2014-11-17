package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.QBPrivateChatManager;
import com.quickblox.chat.listeners.QBPrivateChatManagerListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.File;
import java.util.ArrayList;

public class QBPrivateChatHelper extends BaseChatHelper implements QBPrivateChatManagerListener {

    private static final String TAG = QBPrivateChatHelper.class.getSimpleName();
    private QBPrivateChatManager privateChatManager;
    private QBNotificationChatListener notificationChatListener;
    private QBDialog currentDialog;

    public QBPrivateChatHelper(Context context) {
        super(context);
        notificationChatListener = new PrivateChatNotificationListener();
        addNotificationChatListener(notificationChatListener);
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
        } else {
            savePrivateMessageToCache(chatMessage, dialogId);
        }
    }

    private void savePrivateMessageToCache(QBChatMessage chatMessage, String dialogId) {
        String messageId = chatMessage.getId();
        int friendsMessageTypeCode;
        long time = Long.parseLong(chatMessage.getProperty(ChatUtils.PROPERTY_DATE_SENT).toString());
        String attachUrl = ChatUtils.getAttachUrlFromMessage(new ArrayList<QBAttachment>(chatMessage.getAttachments()));
        MessageCache messageCache = new MessageCache(messageId, dialogId, chatCreator.getId(),
                chatMessage.getBody(), attachUrl, time, false, false, true);

        if (chatMessage.getProperty(ChatUtils.PROPERTY_NOTIFICATION_TYPE) != null) {
            friendsMessageTypeCode = Integer.parseInt(chatMessage.getProperty(
                    ChatUtils.PROPERTY_NOTIFICATION_TYPE).toString());
            if (ChatUtils.isFriendsMessageTypeCode(friendsMessageTypeCode)) {
                messageCache.setMessagesNotificationType(MessagesNotificationType.parseByCode(
                        friendsMessageTypeCode));
            }
        }

        saveMessageToCache(messageCache);
    }

    private void sendPrivateMessage(QBFile file, String message, int userId) throws QBResponseException {
        QBChatMessage chatMessage;
        chatMessage = getQBChatMessage(message, file);
        String dialogId = null;
        if (currentDialog != null) {
            dialogId = currentDialog.getDialogId();
        }
        sendPrivateMessage(chatMessage, userId, dialogId);
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
    }

    @Override
    protected void onPrivateMessageReceived(QBPrivateChat privateChat, QBChatMessage chatMessage) {
        User user = DatabaseManager.getUserById(context, chatMessage.getSenderId());

        if (user == null) {
            user = new User();
            user.setFullName(ConstsCore.EMPTY_STRING + chatMessage.getSenderId());
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
        sendPrivateMessage(chatMessage, opponentId, ConstsCore.EMPTY_STRING);
    }

    @Override
    public void chatCreated(QBPrivateChat privateChat, boolean createdLocally) {
        privateChat.addMessageListener(privateChatMessageListener);
        privateChat.addIsTypingListener(privateChatIsTypingListener);
    }

    public void updateDialog(QBDialog dialog) {
        int countUnreadDialog = DatabaseManager.getCountUnreadMessagesByDialogIdLocal(context,
                dialog.getDialogId());
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
        long time = DateUtilsCore.getCurrentTime();
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, opponentId);
        QBPrivateChat chat = createChat(opponentId);
        QBChatMessage chatMessage = ChatUtils.createChatNotificationMessageToPrivateChat(context, dialog);
        chatMessage.setProperty(ChatUtils.PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
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
        time = DateUtilsCore.getCurrentTime();
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
                friendRequestMessageReceived(chatMessage, MessagesNotificationType.FRIENDS_REQUEST);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_ACCEPT_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, MessagesNotificationType.FRIENDS_ACCEPT);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REJECT_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, MessagesNotificationType.FRIENDS_REJECT);
            } else if (ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REMOVE_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, MessagesNotificationType.FRIENDS_REMOVE);
            } else {
                updateDialogByNotification(chatMessage);
            }
        }
    }

    private void friendRequestMessageReceived(QBChatMessage chatMessage, MessagesNotificationType type) {
        MessageCache messageCache = parseReceivedMessage(chatMessage);
        messageCache.setMessagesNotificationType(type);
        saveMessageToCache(messageCache);
    }
}