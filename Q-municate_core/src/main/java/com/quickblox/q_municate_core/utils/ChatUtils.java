package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.text.TextUtils;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ChatUtils {

    public static final String OCCUPANT_IDS_DIVIDER = ",";

    public static String getAttachUrlFromMessage(Collection<QBAttachment> attachmentsCollection) {
        if (attachmentsCollection != null) {
            ArrayList<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(attachmentsCollection);
            if (!attachmentsList.isEmpty()) {
                return attachmentsList.get(0).getUrl();
            }
        }
        return ConstsCore.EMPTY_STRING;
    }

    public static ArrayList<Integer> createOccupantsIdsFromPrivateMessage(int currentUserId, int senderId) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<Integer>(2);
        occupantsIdsList.add(currentUserId);
        occupantsIdsList.add(senderId);
        return occupantsIdsList;
    }

    public static ArrayList<Integer> getOccupantsIdsListFromString(String occupantIds) {
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        String[] occupantIdsArray = occupantIds.split(OCCUPANT_IDS_DIVIDER);
        for (String occupantId : occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId));
        }
        return occupantIdsList;
    }

    public static ArrayList<Integer> getOccupantIdsWithUser(List<Integer> friendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>(friendIdsList);
        occupantIdsList.add(user.getId());
        return occupantIdsList;
    }

    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, occupantIdsList);
    }

    public static String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = ConstsCore.EMPTY_STRING;
        Collection<QBAttachment> attachmentCollection = chatMessage.getAttachments();
        if (attachmentCollection != null && attachmentCollection.size() > 0) {
            attachURL = getAttachUrlFromMessage(attachmentCollection);
        }
        return attachURL;
    }

    public static QBDialog getExistPrivateDialog(int opponentId) {
        DialogOccupant dialogOccupant = DataManager.getInstance().getDialogOccupantDataManager()
                .getDialogOccupantForPrivateChat(opponentId);

        if (dialogOccupant != null) {
            Dialog dialog = DataManager.getInstance().getDialogDataManager().getByDialogId(dialogOccupant.getDialog().getDialogId());
            return createQBDialogFromLocalDialog(dialog);
        } else {
            return null;
        }
    }

    public static String getFullNameById(int userId) {
        QBUser qbUser = null;
        try {
            qbUser = QBUsers.getUser(userId);
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
        User user = UserFriendUtils.createLocalUser(qbUser);
        DataManager.getInstance().getUserDataManager().createOrUpdate(user);
        return user.getFullName();
    }

    public static String getFullNamesFromOpponentIds(String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        return getFullNamesFromOpponentIdsList(occupantsIdsList);
    }

    public static String getFullNamesFromOpponentId(Integer userId,
            String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        occupantsIdsList.remove(userId);
        return getFullNamesFromOpponentIdsList(occupantsIdsList);
    }

    private static String getFullNamesFromOpponentIdsList(List<Integer> occupantsIdsList) {
        StringBuilder stringBuilder = new StringBuilder(occupantsIdsList.size());
        for (Integer id : occupantsIdsList) {
            stringBuilder.append(getFullNameById(id)).append(OCCUPANT_IDS_DIVIDER).append(" ");
        }
        return stringBuilder.toString().substring(ConstsCore.ZERO_INT_VALUE, stringBuilder.length() - 2);
    }

    public static ArrayList<ParcelableQBDialog> dialogsToParcelableDialogs(List<QBDialog> dialogList){
        ArrayList<ParcelableQBDialog> parcelableDialogList = new ArrayList<ParcelableQBDialog>(dialogList.size());
        for (QBDialog dialog : dialogList) {
            ParcelableQBDialog parcelableQBDialog = new ParcelableQBDialog(dialog);
            parcelableDialogList.add(parcelableQBDialog);
        }
        return parcelableDialogList;
    }

    public static User getOpponentFromPrivateDialog(User currentUser, List<DialogOccupant> occupantsList) {
        for (DialogOccupant dialogOccupant : occupantsList) {
            if (currentUser.getUserId() != dialogOccupant.getUser().getUserId()) {
                return dialogOccupant.getUser();
            }
        }
        return new User();
    }

    public static Dialog createLocalDialog(QBDialog qbDialog) {
        Dialog dialog = new Dialog();
        dialog.setDialogId(qbDialog.getDialogId());
        dialog.setRoomJid(qbDialog.getRoomJid());
        dialog.setTitle(qbDialog.getName());
        dialog.setPhoto(qbDialog.getPhoto());

        if (QBDialogType.PRIVATE.equals(qbDialog.getType())) {
            dialog.setType(Dialog.Type.PRIVATE);
        } else if (QBDialogType.GROUP.equals(qbDialog.getType())){
            dialog.setType(Dialog.Type.GROUP);
        }

        return dialog;
    }

    public static List<Dialog> createLocalDialogsList(List<QBDialog> qbDialogsList) {
        List<Dialog> dialogsList = new ArrayList<>(qbDialogsList.size());

        for (QBDialog qbDialog : qbDialogsList) {
            dialogsList.add(createLocalDialog(qbDialog));
        }

        return dialogsList;
    }

    public static List<DialogOccupant> createDialogOccupantsList(QBDialog qbDialog) {
        List<DialogOccupant> dialogOccupantsList = new ArrayList<>(qbDialog.getOccupants().size());

        for (Integer userId : qbDialog.getOccupants()) {
            DialogOccupant dialogOccupant = new DialogOccupant();
            dialogOccupant.setUser(DataManager.getInstance().getUserDataManager().get(userId));
            dialogOccupant.setDialog(DataManager.getInstance().getDialogDataManager().getByDialogId(qbDialog.getDialogId()));

            dialogOccupantsList.add(dialogOccupant);
        }

        return dialogOccupantsList;
    }

    public static List<Integer> getIdsFromDialogOccupantsList(List<DialogOccupant> dialogOccupantsList) {
        List<Integer> idsList = new ArrayList<>(dialogOccupantsList.size());

        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            idsList.add(dialogOccupant.getDialogOccupantId());
        }

        return idsList;
    }

    public static QBDialog createQBDialogFromLocalDialog(Dialog dialog) {
        QBDialog qbDialog = new QBDialog();
        qbDialog.setDialogId(dialog.getDialogId());
        qbDialog.setRoomJid(dialog.getRoomJid());
        qbDialog.setPhoto(dialog.getPhoto());
        qbDialog.setName(dialog.getTitle());
        List<DialogOccupant> dialogOccupantsList = DataManager.getInstance().getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        qbDialog.setOccupantsIds(createOccupantsIdsFromDialogOccupantsList(dialogOccupantsList));
        qbDialog.setType(Dialog.Type.PRIVATE.equals(
                dialog.getType()) ? QBDialogType.PRIVATE : QBDialogType.GROUP);
        return qbDialog;
    }

    private static ArrayList<Integer> createOccupantsIdsFromDialogOccupantsList(
            List<DialogOccupant> dialogOccupantsList) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<>(dialogOccupantsList.size());
        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            occupantsIdsList.add(dialogOccupant.getUser().getUserId());
        }
        return occupantsIdsList;
    }

    public static Message createLocalMessage(QBChatMessage qbChatMessage, DialogOccupant dialogOccupant, State state) {
        long dateSent = getMessageDateSent(qbChatMessage);
        Message message = new Message();
        message.setMessageId(qbChatMessage.getId());
        message.setDialogOccupant(dialogOccupant);
        message.setCreatedDate(dateSent);
        if (!message.isIncoming(AppSession.getSession().getUser().getId())) {
            message.setState(null);
        } else if (state == null) {
            message.setState(qbChatMessage.isRead() ? State.READ : State.DELIVERED);
        } else {
            message.setState(state);
        }
        message.setBody(qbChatMessage.getBody());
        return message;
    }

    public static long getMessageDateSent(QBChatMessage qbChatMessage) {
        String dateSentString = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DATE_SENT);
        return dateSentString != null ? Long.parseLong(dateSentString) : qbChatMessage.getDateSent();
    }

    public static Attachment createLocalAttachment(QBAttachment qbAttachment) {
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(qbAttachment.getId());
        attachment.setRemoteUrl(qbAttachment.getUrl());
        attachment.setName(qbAttachment.getName());
        attachment.setSize(qbAttachment.getSize());
        return attachment;
    }

    public static DialogNotification createLocalDialogNotification(Context context, QBChatMessage qbChatMessage, DialogOccupant dialogOccupant) {
        DialogNotification dialogNotification = new DialogNotification();
        dialogNotification.setDialogNotificationId(qbChatMessage.getId());
        dialogNotification.setDialogOccupant(dialogOccupant);

        int friendsMessageTypeCode = Integer.parseInt(qbChatMessage.getProperty(
                ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE).toString());
        if (ChatNotificationUtils.isFriendsNotificationMessage(friendsMessageTypeCode)) {
            dialogNotification.setType(DialogNotification.Type.parseByCode(friendsMessageTypeCode));
            dialogNotification.setBody(ChatNotificationUtils.getBodyForFriendsNotificationMessage(context,
                    DialogNotification.Type.parseByCode(friendsMessageTypeCode),
                    qbChatMessage));
        } else if (ChatNotificationUtils.PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE.equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING)
                || ChatNotificationUtils.PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE.equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING)) {
            dialogNotification.setBody(ChatNotificationUtils.getBodyForUpdateChatNotificationMessage(context,
                    qbChatMessage));
            dialogNotification.setType(ChatNotificationUtils.getUpdateChatNotificationMessageType(
                    qbChatMessage));
        }

        long dateSent = getMessageDateSent(qbChatMessage);
        dialogNotification.setCreatedDate(dateSent);
        dialogNotification.setState(qbChatMessage.isRead() ? State.READ : State.DELIVERED);

        return dialogNotification;
    }

    public static List<CombinationMessage> getCombinationMessagesListFromMessagesList(List<Message> messagesList) {
        List<CombinationMessage> combinationMessagesList = new ArrayList<>(messagesList.size());

        for (Message message : messagesList) {
            combinationMessagesList.add(new CombinationMessage(message));
        }

        return combinationMessagesList;
    }

    public static List<CombinationMessage> getCombinationMessagesListFromDialogNotificationsList(List<DialogNotification> dialogNotificationsList) {
        List<CombinationMessage> combinationMessagesList = new ArrayList<>(dialogNotificationsList.size());

        for (DialogNotification dialogNotification : dialogNotificationsList) {
            combinationMessagesList.add(new CombinationMessage(dialogNotification));
        }

        return combinationMessagesList;
    }

    public static DialogNotification convertMessageToDialogNotification(Message message) {
        DialogNotification dialogNotification = new DialogNotification();
        dialogNotification.setDialogNotificationId(message.getMessageId());
        dialogNotification.setDialogOccupant(message.getDialogOccupant());
        dialogNotification.setBody(message.getBody());
        dialogNotification.setCreatedDate(message.getCreatedDate());
        dialogNotification.setState(message.getState());
        return dialogNotification;
    }

    public static List<DialogNotification> readAllDialogNotification(List<DialogNotification> dialogNotificationsList, QBUser currentQbUser) {
        List<DialogNotification> updateDialogNotificationsList = new ArrayList<>(dialogNotificationsList.size());

        for (DialogNotification dialogNotification : dialogNotificationsList) {
            if (!State.READ.equals(dialogNotification.getState())
                    && currentQbUser.getId() != dialogNotification.getDialogOccupant().getUser().getUserId()) {
                dialogNotification.setState(State.READ);
                updateDialogNotificationsList.add(dialogNotification);
            }
        }

        return updateDialogNotificationsList;
    }

    public static List<Message> readAllMessages(List<Message> messagesList, QBUser currentQbUser) {
        List<Message> updateMessagesList = new ArrayList<>(messagesList.size());

        for (Message message : messagesList) {
            if (!State.READ.equals(message.getState())
                    && currentQbUser.getId() != message.getDialogOccupant().getUser().getUserId()) {
                message.setState(State.READ);
                updateMessagesList.add(message);
            }
        }

        return updateMessagesList;
    }

    public static List<CombinationMessage> createCombinationMessagesList(List<Message> messagesList,
            List<DialogNotification> dialogNotificationsList) {
        List<CombinationMessage> combinationMessagesList = new ArrayList<>();
        combinationMessagesList.addAll(ChatUtils.getCombinationMessagesListFromMessagesList(messagesList));
        combinationMessagesList.addAll(ChatUtils.getCombinationMessagesListFromDialogNotificationsList(
                dialogNotificationsList));
        Collections.sort(combinationMessagesList, new CombinationMessage.DateComparator());
        return combinationMessagesList;
    }
}