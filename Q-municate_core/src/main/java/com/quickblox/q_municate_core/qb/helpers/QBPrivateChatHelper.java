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
import com.quickblox.q_municate_db.managers.DataManager;
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

    private QBNotificationChatListener notificationChatListener;

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
    public QBPrivateChat createChatLocally(QBDialog dialog, Bundle additional) throws QBResponseException {
        int opponentId = additional.getInt(QBServiceConsts.EXTRA_OPPONENT_ID);
        QBPrivateChat privateChat = createPrivateChatIfNotExist(opponentId);
        currentDialog = dialog;
        return privateChat;
    }

    @Override
    public void closeChat(QBDialog dialogId, Bundle additional) {
        currentDialog = null;
    }

    public void init(QBUser user) {
        super.init(user);
    }

    public void onPrivateMessageReceived(QBChat chat, QBChatMessage qbChatMessage) {
        User user = DataManager.getInstance().getUserDataManager().get(qbChatMessage.getSenderId());
        saveMessageToCache(qbChatMessage.getDialogId(), qbChatMessage, State.DELIVERED);
        notifyMessageReceived(qbChatMessage, user, qbChatMessage.getDialogId(), true);
    }

    public void updateDialog(QBDialog dialog) {
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

    private void friendRequestMessageReceived(QBChatMessage qbChatMessage,
            DialogNotification.Type notificationType) {
        Message message = parseReceivedMessage(qbChatMessage);
        DialogNotification dialogNotification = ChatUtils.convertMessageToDialogNotification(message);
        dialogNotification.setType(notificationType);

        Dialog dialog = dataManager.getDialogDataManager().getByDialogId(qbChatMessage.getDialogId());
        if (dialog == null) {
            QBDialog qbDialog = ChatNotificationUtils.parseDialogFromQBMessage(context, qbChatMessage,
                    QBDialogType.PRIVATE);
            ArrayList<Integer> occupantsIdsList = ChatUtils.createOccupantsIdsFromPrivateMessage(
                    chatCreator.getId(), qbChatMessage.getSenderId());
            qbDialog.setOccupantsIds(occupantsIdsList);
            saveDialogToCache(qbDialog);
        }

        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(qbChatMessage.getDialogId(),
                qbChatMessage.getSenderId());
        saveDialogNotificationToCache(dialogOccupant, qbChatMessage);
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