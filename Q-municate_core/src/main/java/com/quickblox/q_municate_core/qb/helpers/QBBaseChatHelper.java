package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

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
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;
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
    protected DataManager dataManager;
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

        dataManager = DataManager.getInstance();
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

    public void sendPrivateMessage(QBChatMessage qbChatMessage, int opponentId, String dialogId) throws QBResponseException {
        addNecessaryPropertyForQBChatMessage(qbChatMessage, dialogId);

        sendPrivateMessage(qbChatMessage, opponentId);
        saveMessageToCache(dialogId, qbChatMessage, null, true);
    }

    public void sendPrivateMessage(QBChatMessage qbChatMessage, int opponentId) throws QBResponseException {
        QBPrivateChat privateChat = createPrivateChatIfNotExist(opponentId);

        qbChatMessage.setMarkable(true);

        String error = null;
        try {
            if (privateChat != null) {
                privateChat.sendMessage(qbChatMessage);
            }
        } catch (XMPPException | SmackException.NotConnectedException e) {
            error = context.getString(R.string.dlg_fail_connection);
        }
        if (error != null) {
            throw new QBResponseException(error);
        }
    }

    protected void addNecessaryPropertyForQBChatMessage(QBChatMessage qbChatMessage, String dialogId) {
        long time = DateUtilsCore.getCurrentTime();
        qbChatMessage.setProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID, dialogId);
        qbChatMessage.setProperty(ChatNotificationUtils.PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
    }

    public void saveDialogToCache(QBDialog qbDialog) {
        Dialog dialog = ChatUtils.createLocalDialog(qbDialog);
        dataManager.getDialogDataManager().createOrUpdate(dialog);

        saveDialogsOccupants(qbDialog);
    }

    public void saveDialogsToCache(List<QBDialog> qbDialogsList) {
        dataManager.getDialogDataManager().createOrUpdate(ChatUtils.createLocalDialogsList(qbDialogsList));

        saveDialogsOccupants(qbDialogsList);

        saveTempMessages(qbDialogsList);
    }

    private void saveTempMessages(List<QBDialog> qbDialogsList) {
        dataManager.getMessageDataManager().createOrUpdate(
                ChatUtils.createTempLocalMessagesList(qbDialogsList));
    }

    protected void saveTempMessage(Message message) {
        dataManager.getMessageDataManager().createOrUpdate(message);
        updateDialogModifiedDate(message.getDialogOccupant().getDialog(), message.getCreatedDate(), false);
    }

    protected List<DialogOccupant> saveDialogsOccupants(QBDialog qbDialog) {
        List<DialogOccupant> dialogOccupantsList = ChatUtils.createDialogOccupantsList(qbDialog);
        dataManager.getDialogOccupantDataManager().createOrUpdate(dialogOccupantsList);
        return dialogOccupantsList;
    }

    protected void saveDialogOccupant(DialogOccupant dialogOccupant) {
        dataManager.getDialogOccupantDataManager().createOrUpdate(dialogOccupant);
    }

    private void saveDialogsOccupants(List<QBDialog> qbDialogsList) {
        for (QBDialog qbDialog : qbDialogsList) {
            saveDialogsOccupants(qbDialog);
        }
    }

    public List<QBDialog> getDialogs() throws Exception {
        Bundle bundle = new Bundle();
        QBRequestGetBuilder customObjectRequestBuilder = new QBRequestGetBuilder();
        customObjectRequestBuilder.setPagesLimit(ConstsCore.CHATS_DIALOGS_PER_PAGE);
        List<QBDialog> qbDialogsList = QBChatService.getChatDialogs(null, customObjectRequestBuilder, bundle);

        new FinderUnknownUsers(context, chatCreator, qbDialogsList).find();

        saveDialogsToCache(qbDialogsList);

        return qbDialogsList;
    }

    public List<QBChatMessage> getDialogMessages(QBRequestGetBuilder customObjectRequestBuilder,
                                                        Bundle returnedBundle, QBDialog qbDialog,
                                                        long lastDateLoad) throws QBResponseException {
        List<QBChatMessage> qbMessagesList = QBChatService.getDialogMessages(qbDialog,
                customObjectRequestBuilder, returnedBundle);

        if (qbMessagesList != null && !qbMessagesList.isEmpty()) {
            saveMessagesToCache(qbMessagesList, qbDialog.getDialogId());
        }

        return qbMessagesList;
    }

    protected void saveMessagesToCache(List<QBChatMessage> qbMessagesList, String dialogId) {
        for (int i = 0; i < qbMessagesList.size(); i++) {
            QBChatMessage qbChatMessage = qbMessagesList.get(i);
            boolean notify = i == qbMessagesList.size() - 1;
            saveMessageToCache(dialogId, qbChatMessage, null, notify);
        }
    }

    protected void saveMessageToCache(String dialogId, QBChatMessage qbChatMessage, State state, boolean notify) {
        DialogOccupant dialogOccupant;
        if (qbChatMessage.getSenderId() == null) {
            dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, chatCreator.getId());
        } else {
            dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        }

        boolean isDialogNotification = qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE) != null;
        if (isDialogNotification) {
            saveDialogNotificationToCache(dialogOccupant, qbChatMessage, notify);
        } else {
            Message message = ChatUtils.createLocalMessage(qbChatMessage, dialogOccupant, state);
            if (qbChatMessage.getAttachments() != null && !qbChatMessage.getAttachments().isEmpty()) {
                ArrayList<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(qbChatMessage.getAttachments());
                Attachment attachment = ChatUtils.createLocalAttachment(attachmentsList.get(0));
                message.setAttachment(attachment);

                dataManager.getAttachmentDataManager().createOrUpdate(attachment, notify);
            }

            dataManager.getMessageDataManager().createOrUpdate(message, notify);
            updateDialogModifiedDate(dialogOccupant.getDialog(), message.getCreatedDate(), notify);
        }
    }

    protected void updateDialogModifiedDate(Dialog dialog, long modifiedDate, boolean notify) {
        if (dialog != null) {
            dialog.setModifiedDate(modifiedDate);
            dataManager.getDialogDataManager().update(dialog, notify);
        }
    }

    protected void saveDialogNotificationToCache(DialogOccupant dialogOccupant, QBChatMessage qbChatMessage, boolean notify) {
        DialogNotification dialogNotification = ChatUtils.createLocalDialogNotification(context, qbChatMessage, dialogOccupant);
        saveDialogNotificationToCache(dialogNotification, notify);
    }

    protected void saveDialogNotificationToCache(DialogNotification dialogNotification, boolean notify) {
        dataManager.getDialogNotificationDataManager().createOrUpdate(dialogNotification, notify);
        updateDialogModifiedDate(dialogNotification.getDialogOccupant().getDialog(), dialogNotification.getCreatedDate(), notify);
    }

    private void deleteDialogLocal(String dialogId) {
        dataManager.getDialogDataManager().deleteById(dialogId);
    }

    public void deleteDialog(String dialogId, Dialog.Type dialogType) {
        try {
            if (Dialog.Type.PRIVATE.equals(dialogType)) {
                QBChatService.getInstance().getPrivateChatManager().deleteDialog(dialogId);
            } else {
                QBChatService.getInstance().getGroupChatManager().deleteDialog(dialogId);
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        deleteDialogLocal(dialogId);
    }

    protected QBChatMessage getQBChatMessage(String body, QBFile qbFile) {
        long time = DateUtilsCore.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);

        if (qbFile != null) {
            QBAttachment attachment = getAttachment(qbFile);
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

    public void sendTypingStatusToServer(int opponentId, boolean startTyping)  {
        try {
            QBPrivateChat privateChat = createPrivateChatIfNotExist(opponentId);
            if (startTyping) {
                privateChat.sendIsTypingNotification();
            } else {
                privateChat.sendStopTypingNotification();
            }
        } catch (XMPPException | SmackException.NotConnectedException | QBResponseException e) {
            ErrorUtils.logError(e);
        }
    }

    public QBPrivateChat createPrivateChatIfNotExist(int userId) throws QBResponseException {
        if (privateChatManager == null) {
            ErrorUtils.logError(TAG, " private chat is NULL");
            throw new QBResponseException(context.getString(R.string.dlg_fail_create_chat));
        }

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

    public QBDialog createPrivateDialogIfNotExist(int userId) throws QBResponseException {
        QBDialog existingPrivateDialog = ChatUtils.getExistPrivateDialog(userId);
        if (existingPrivateDialog == null) {
            existingPrivateDialog = createPrivateChatOnRest(userId);
            saveDialogToCache(existingPrivateDialog);
        }
        return existingPrivateDialog;
    }

    protected void checkForSendingNotification(boolean ownMessage, QBChatMessage qbChatMessage, User user,
            boolean isPrivateChat) {
        String dialogId = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        if (qbChatMessage.getId() == null || dialogId == null) {
            return;
        }

        sendNotificationBroadcast(QBServiceConsts.GOT_CHAT_MESSAGE, qbChatMessage, user, dialogId, isPrivateChat);

        if (currentDialog != null) {
            if (!ownMessage && !currentDialog.getDialogId().equals(dialogId)) {
                sendNotificationBroadcast(QBServiceConsts.GOT_CHAT_MESSAGE_LOCAL, qbChatMessage, user, dialogId, isPrivateChat);
            }
        } else {
            sendNotificationBroadcast(QBServiceConsts.GOT_CHAT_MESSAGE_LOCAL, qbChatMessage, user, dialogId, isPrivateChat);
        }
    }

    private void sendNotificationBroadcast(String action, QBChatMessage chatMessage, User user, String dialogId,
            boolean isPrivateMessage) {
        Intent intent = new Intent(action);
        String messageBody = chatMessage.getBody();
        String extraChatMessage;

        if (chatMessage.getAttachments() != null && !chatMessage.getAttachments().isEmpty()) {
            extraChatMessage = context.getResources().getString(R.string.file_was_attached);
        } else {
            extraChatMessage = messageBody;
        }

        intent.putExtra(QBServiceConsts.EXTRA_CHAT_MESSAGE, extraChatMessage);
        intent.putExtra(QBServiceConsts.EXTRA_USER, user);
        intent.putExtra(QBServiceConsts.EXTRA_DIALOG_ID, dialogId);
        intent.putExtra(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE, isPrivateMessage);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        context.sendBroadcast(intent);
    }

    protected void notifyMessageTyping(int userId, boolean isTyping) {
        Intent intent = new Intent(QBServiceConsts.TYPING_MESSAGE);
        intent.putExtra(QBServiceConsts.EXTRA_USER_ID, userId);
        intent.putExtra(QBServiceConsts.EXTRA_IS_TYPING, isTyping);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected Message parseReceivedMessage(QBChatMessage qbChatMessage) {
        String dateSentString = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DATE_SENT);
        long dateSent = dateSentString != null ? Long.parseLong(dateSentString) : qbChatMessage.getDateSent();
        String attachUrl = ChatUtils.getAttachUrlIfExists(qbChatMessage);
        String dialogId = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);

        Message message = new Message();
        message.setMessageId(qbChatMessage.getId());
        message.setBody(qbChatMessage.getBody());
        message.setCreatedDate(dateSent);
        message.setState(State.DELIVERED);

        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        if (dialogOccupant == null) {
            dialogOccupant = new DialogOccupant();
            Dialog dialog = dataManager.getDialogDataManager().getByDialogId(dialogId);
            if (dialog != null) {
                dialogOccupant.setDialog(dialog);
            }
            User user = dataManager.getUserDataManager().get(qbChatMessage.getSenderId());
            if (user != null) {
                dialogOccupant.setUser(user);
            }
        }

        message.setDialogOccupant(dialogOccupant);

        if (qbChatMessage.getAttachments()!= null && !qbChatMessage.getAttachments().isEmpty()) {
            Attachment attachment = new Attachment();
            attachment.setType(Attachment.Type.PICTURE);
            attachment.setRemoteUrl(attachUrl);
            message.setAttachment(attachment);
        }

        return message;
    }

    public void updateStatusNotificationMessageRead(String dialogId, CombinationMessage combinationMessage) throws Exception {
        updateStatusMessageReadServer(dialogId, combinationMessage, false);
        updateStatusNotificationMessageLocal(combinationMessage.toDialogNotification());
    }

    public void updateStatusMessageRead(String dialogId, CombinationMessage combinationMessage,
            boolean forPrivate) throws Exception {
        updateStatusMessageReadServer(dialogId, combinationMessage, forPrivate);
        updateStatusMessageLocal(combinationMessage.toMessage());
    }

    public void updateStatusMessageReadServer(String dialogId, CombinationMessage combinationMessage,
            boolean fromPrivate) throws Exception {
        StringifyArrayList<String> messagesIdsList = new StringifyArrayList<String>();
        messagesIdsList.add(combinationMessage.getMessageId());
        QBChatService.markMessagesAsRead(dialogId, messagesIdsList);

        if (fromPrivate) {
            QBPrivateChat privateChat = createPrivateChatIfNotExist(combinationMessage.getDialogOccupant().getUser().getUserId());
            if (privateChat != null) {
                privateChat.readMessage(combinationMessage.getMessageId());
            }
        }
    }

    public void updateStatusMessageLocal(Message message) {
        dataManager.getMessageDataManager().update(message, false);
    }

    public void updateStatusNotificationMessageLocal(DialogNotification dialogNotification) {
        dataManager.getDialogNotificationDataManager().update(dialogNotification, false);
    }

    public void updateStatusMessageLocal(String messageId, State state) {
        Message message = dataManager.getMessageDataManager().getByMessageId(messageId);
        if (message != null) {
            message.setState(state);
            dataManager.getMessageDataManager().update(message);
        }
    }

    public void onGroupMessageReceived(QBChat groupChat, final QBChatMessage chatMessage) {
    }

    public void onPrivateMessageReceived(QBChat privateChat, final QBChatMessage chatMessage) {
    }

    public interface QBNotificationChatListener {

        void onReceivedNotification(String notificationType, QBChatMessage chatMessage);
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
                    notificationChatListener.onReceivedNotification((String) chatMessage.getProperty(
                            ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE), chatMessage);
                }
            } else {
                onPrivateMessageReceived(privateChat, chatMessage);
            }
        }

        @Override
        public void processError(QBPrivateChat privateChat, QBChatException error, QBChatMessage originMessage) {
            // TODO: need to be implemented
        }

        @Override
        public void processMessageDelivered(QBPrivateChat privateChat, String messageId) {
            updateStatusMessageLocal(messageId, State.DELIVERED);
        }

        @Override
        public void processMessageRead(QBPrivateChat privateChat, String messageId) {
            updateStatusMessageLocal(messageId, State.READ);
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
            notifyMessageTyping(privateChat.getParticipant(), true);
        }

        @Override
        public void processUserStopTyping(QBPrivateChat privateChat) {
            notifyMessageTyping(privateChat.getParticipant(), false);
        }
    }
}