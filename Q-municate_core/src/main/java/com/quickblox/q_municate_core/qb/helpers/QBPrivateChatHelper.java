package com.quickblox.q_municate_core.qb.helpers;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.chat.QBChat;
import com.quickblox.chat.QBPrivateChat;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.content.QBContent;
import com.quickblox.content.model.QBFile;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;

import java.io.File;
import java.util.ArrayList;

public class QBPrivateChatHelper extends QBBaseChatHelper {

    private static final String TAG = QBPrivateChatHelper.class.getSimpleName();

    public QBPrivateChatHelper(Context context) {
        super(context);
        QBNotificationChatListener notificationChatListener = new PrivateChatNotificationListener();
        addNotificationChatListener(notificationChatListener);
    }

    public void init(QBUser user) {
        super.init(user);
    }

    @Override
    public synchronized QBPrivateChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException {
        currentDialog = dialog;
        int opponentId = additional.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        return createPrivateChatIfNotExist(opponentId);
    }

    @Override
    public synchronized void closeChat(QBDialog qbDialog, Bundle additional) {
        if (currentDialog != null && currentDialog.getDialogId().equals(qbDialog.getDialogId())) {
            currentDialog = null;
        }
    }

    public void sendPrivateMessage(String message, int userId) throws QBResponseException {
        sendPrivateMessage(null, message, userId);
    }

    public void sendPrivateMessageWithAttachImage(QBFile file, int userId) throws QBResponseException {
        sendPrivateMessage(file, context.getString(R.string.dlg_attached_last_message), userId);
    }

    private void sendPrivateMessage(QBFile file, String message, int userId) throws QBResponseException {
        QBChatMessage qbChatMessage = getQBChatMessage(message, file);
        String dialogId = null;
        if (currentDialog != null) {
            dialogId = currentDialog.getDialogId();
        }
        sendPrivateMessage(qbChatMessage, userId, dialogId);
    }

    public void onPrivateMessageReceived(QBChat chat, QBChatMessage qbChatMessage) {
        String dialogId = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        if (qbChatMessage.getId() != null && dialogId != null) {
            User user = dataManager.getUserDataManager().get(qbChatMessage.getSenderId());
            ChatUtils.saveMessageToCache(context, dataManager, dialogId, qbChatMessage, State.DELIVERED, true);

            checkForSendingNotification(false, qbChatMessage, user, true);
        }
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

    private void friendRequestMessageReceived(QBChatMessage qbChatMessage, DialogNotification.Type notificationType) {
        String dialogId = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DIALOG_ID);
        Message message = parseReceivedMessage(qbChatMessage);
        DialogNotification dialogNotification = ChatUtils.convertMessageToDialogNotification(message);
        dialogNotification.setType(notificationType);

        Dialog dialog = dataManager.getDialogDataManager().getByDialogId(dialogId);
        if (dialog == null) {
            QBDialog qbDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, dataManager, qbChatMessage, QBDialogType.PRIVATE);
            ArrayList<Integer> occupantsIdsList = ChatUtils.createOccupantsIdsFromPrivateMessage(chatCreator.getId(), qbChatMessage.getSenderId());
            qbDialog.setOccupantsIds(occupantsIdsList);
            ChatUtils.saveDialogToCache(dataManager, qbDialog);
        }

        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        ChatUtils.saveDialogNotificationToCache(context, dataManager, dialogOccupant, qbChatMessage, true);
    }

    private class PrivateChatNotificationListener implements QBNotificationChatListener {

        @Override
        public void onReceivedNotification(String notificationType, QBChatMessage chatMessage) {
            if (ChatNotificationUtils.PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REQUEST.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_REQUEST);
            } else if (ChatNotificationUtils.PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_ACCEPT.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_ACCEPT);
            } else if (ChatNotificationUtils.PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REJECT.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_REJECT);
            } else if (ChatNotificationUtils.PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REMOVE.equals(notificationType)) {
                friendRequestMessageReceived(chatMessage, DialogNotification.Type.FRIENDS_REMOVE);
            }
        }
    }
}