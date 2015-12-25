package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;

import java.util.ArrayList;
import java.util.List;

public class DbUtils {

    public static DialogOccupant saveDialogOccupantIfUserNotExists(DataManager dataManager,
            String dialogId, int userId, DialogOccupant.Status status) {
        QBRestHelper.loadAndSaveUser(userId);

        User user = DataManager.getInstance().getUserDataManager().get(userId);
        DialogOccupant dialogOccupant = ChatUtils.createDialogOccupant(dataManager, dialogId, user);
        dialogOccupant.setStatus(status);

        saveDialogOccupant(dataManager, dialogOccupant);

        return dialogOccupant;
    }

    public static void saveDialogToCache(DataManager dataManager, QBDialog qbDialog) {
        Dialog dialog = ChatUtils.createLocalDialog(qbDialog);
        dataManager.getDialogDataManager().createOrUpdate(dialog);

        if (qbDialog.getOccupants() != null && !qbDialog.getOccupants().isEmpty()) {
            saveDialogsOccupants(dataManager, qbDialog, false);
        }
    }

    public static void saveDialogsToCache(DataManager dataManager, List<QBDialog> qbDialogsList,
            QBDialog currentDialog) {
        dataManager.getDialogDataManager().createOrUpdateAll(ChatUtils.createLocalDialogsList(qbDialogsList));

        saveDialogsOccupants(dataManager, qbDialogsList);

        saveTempMessages(dataManager, qbDialogsList, currentDialog);
    }

    public static void saveTempMessages(DataManager dataManager, List<QBDialog> qbDialogsList,
            QBDialog currentDialog) {
        dataManager.getMessageDataManager()
                .createOrUpdateAll(ChatUtils.createTempLocalMessagesList(dataManager, qbDialogsList, currentDialog));
    }

    public static void saveTempMessage(DataManager dataManager, Message message) {
        dataManager.getMessageDataManager().createOrUpdate(message);
        updateDialogModifiedDate(dataManager, message.getDialogOccupant().getDialog().getDialogId(),
                message.getCreatedDate(), true);
    }

    public static List<DialogOccupant> saveDialogsOccupants(DataManager dataManager, QBDialog qbDialog, boolean onlyNewOccupant) {
        List<DialogOccupant> dialogOccupantsList = ChatUtils.createDialogOccupantsList(dataManager, qbDialog, onlyNewOccupant);
        if (!dialogOccupantsList.isEmpty()) {
            dataManager.getDialogOccupantDataManager().createOrUpdateAll(dialogOccupantsList);
        }
        return dialogOccupantsList;
    }

    public static void saveDialogOccupant(DataManager dataManager, DialogOccupant dialogOccupant) {
        dataManager.getDialogOccupantDataManager().createOrUpdate(dialogOccupant);
    }

    public static void saveDialogsOccupants(DataManager dataManager, List<QBDialog> qbDialogsList) {
        for (QBDialog qbDialog : qbDialogsList) {
            saveDialogsOccupants(dataManager, qbDialog, false);
        }
    }

    public static void updateStatusMessageLocal(DataManager dataManager, Message message) {
        dataManager.getMessageDataManager().update(message, false);
    }

    public static void updateStatusNotificationMessageLocal(DataManager dataManager,
            DialogNotification dialogNotification) {
        dataManager.getDialogNotificationDataManager().update(dialogNotification, false);
    }

    public static void updateStatusMessageLocal(DataManager dataManager, String messageId, State state) {
        Message message = dataManager.getMessageDataManager().getByMessageId(messageId);
        if (message != null && !state.equals(message.getState())) {
            message.setState(state);
            dataManager.getMessageDataManager().update(message);
        }
    }

    public static void saveMessagesToCache(Context context, DataManager dataManager,
            List<QBChatMessage> qbMessagesList, String dialogId) {
        for (int i = 0; i < qbMessagesList.size(); i++) {
            QBChatMessage qbChatMessage = qbMessagesList.get(i);
            boolean notify = i == qbMessagesList.size() - 1;
            saveMessageOrNotificationToCache(context, dataManager, dialogId, qbChatMessage, null, notify);
        }

        updateDialogModifiedDate(dataManager, dialogId, true);
    }

    public static void saveMessageOrNotificationToCache(Context context, DataManager dataManager,
            String dialogId, QBChatMessage qbChatMessage, State state, boolean notify) {
        DialogOccupant dialogOccupant;
        if (qbChatMessage.getSenderId() == null) {
            dialogOccupant = dataManager.getDialogOccupantDataManager()
                    .getDialogOccupant(dialogId, AppSession.getSession().getUser().getId());
        } else {
            dialogOccupant = dataManager.getDialogOccupantDataManager()
                    .getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        }

        if (dialogOccupant == null && qbChatMessage.getSenderId() != null) {
            saveDialogOccupantIfUserNotExists(dataManager, dialogId, qbChatMessage.getSenderId(),
                    DialogOccupant.Status.DELETED);
            dialogOccupant = dataManager.getDialogOccupantDataManager()
                    .getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        }

        if (ChatNotificationUtils.isNotificationMessage(qbChatMessage)) {
            saveDialogNotificationToCache(context, dataManager, dialogOccupant, qbChatMessage, notify);
        } else {
            Message message = ChatUtils.createLocalMessage(qbChatMessage, dialogOccupant, state);
            if (qbChatMessage.getAttachments() != null && !qbChatMessage.getAttachments().isEmpty()) {
                ArrayList<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(
                        qbChatMessage.getAttachments());
                Attachment attachment = ChatUtils.createLocalAttachment(attachmentsList.get(0));
                message.setAttachment(attachment);

                dataManager.getAttachmentDataManager().createOrUpdate(attachment, notify);
            }

            dataManager.getMessageDataManager().createOrUpdate(message, notify);
        }
    }

    public static void updateDialogModifiedDate(DataManager dataManager, String dialogId, long modifiedDate,
            boolean notify) {
        Dialog dialog = dataManager.getDialogDataManager().getByDialogId(dialogId);
        updateDialogModifiedDate(dataManager, dialog, modifiedDate, notify);
    }

    private static void updateDialogModifiedDate(DataManager dataManager, String dialogId, boolean notify) {
        long modifiedDate = getDialogModifiedDate(dataManager, dialogId);
        updateDialogModifiedDate(dataManager, dialogId, modifiedDate, notify);
    }

    private static void updateDialogModifiedDate(DataManager dataManager, Dialog dialog, long modifiedDate,
            boolean notify) {
        if (dialog != null) {
            dialog.setModifiedDateLocal(modifiedDate);
            dataManager.getDialogDataManager().update(dialog, notify);
        }
    }

    public static long getDialogModifiedDate(DataManager dataManager, String dialogId) {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialogId);
        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);

        Message message = dataManager.getMessageDataManager()
                .getLastMessageByDialogId(dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager()
                .getLastDialogNotificationByDialogId(dialogOccupantsIdsList);

        return ChatUtils.getDialogMessageCreatedDate(true, message, dialogNotification);
    }

    public static void saveDialogNotificationToCache(Context context, DataManager dataManager,
            DialogOccupant dialogOccupant, QBChatMessage qbChatMessage, boolean notify) {
        DialogNotification dialogNotification = ChatUtils.createLocalDialogNotification(context, dataManager,
                qbChatMessage, dialogOccupant);
        saveDialogNotificationToCache(dataManager, dialogNotification, notify);
    }

    private static void saveDialogNotificationToCache(DataManager dataManager,
            DialogNotification dialogNotification, boolean notify) {
        if (dialogNotification.getDialogOccupant() != null) {
            dataManager.getDialogNotificationDataManager().createOrUpdate(dialogNotification, notify);
        }
    }

    public static void deleteDialogLocal(DataManager dataManager, String dialogId) {
        dataManager.getDialogDataManager().deleteById(dialogId);
    }

    public static void updateDialogOccupants(DataManager dataManager, String dialogId,
            List<Integer> dialogOccupantIdsList, DialogOccupant.Status status) {
        List<DialogOccupant> dialogOccupantsList = ChatUtils.
                getUpdatedDialogOccupantsList(dataManager, dialogId, dialogOccupantIdsList, status);
        dataManager.getDialogOccupantDataManager().createOrUpdateAll(dialogOccupantsList);
    }

    public static void updateDialogOccupant(DataManager dataManager, String dialogId,
            int occupantId, DialogOccupant.Status status) {
        DialogOccupant dialogOccupant = ChatUtils.getUpdatedDialogOccupant(dataManager, dialogId, status,
                occupantId);
        dataManager.getDialogOccupantDataManager().update(dialogOccupant);
    }

    public static void updateDialogsOccupantsStatusesIfNeeded(DataManager dataManager, List<QBDialog> qbDialogsList) {
        for (QBDialog qbDialog : qbDialogsList) {
            updateDialogOccupantsStatusesIfNeeded(dataManager, qbDialog);
        }
    }

    public static void updateDialogOccupantsStatusesIfNeeded(DataManager dataManager, QBDialog qbDialog) {
        List<DialogOccupant> oldDialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(qbDialog.getDialogId());
        List<DialogOccupant> updatedDialogOccupantsList = new ArrayList<>();
        List<DialogOccupant> newDialogOccupantsList = dataManager.getDialogOccupantDataManager().getActualDialogOccupantsByIds(
                qbDialog.getDialogId(), qbDialog.getOccupants());

        for (DialogOccupant oldDialogOccupant : oldDialogOccupantsList) {
            if (!newDialogOccupantsList.contains(oldDialogOccupant)) {
                oldDialogOccupant.setStatus(DialogOccupant.Status.DELETED);
                updatedDialogOccupantsList.add(oldDialogOccupant);
            }
        }

        if (!updatedDialogOccupantsList.isEmpty()) {
            dataManager.getDialogOccupantDataManager().updateAll(updatedDialogOccupantsList);
        }
    }
}