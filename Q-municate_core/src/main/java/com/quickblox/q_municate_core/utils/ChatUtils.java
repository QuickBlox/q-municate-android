package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBSettings;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.models.NotificationType;
import com.quickblox.q_municate_core.models.ParcelableQBDialog;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Attachment;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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

    public static String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = ConstsCore.EMPTY_STRING;
        Collection<QBAttachment> attachmentCollection = chatMessage.getAttachments();
        if (attachmentCollection != null && attachmentCollection.size() > 0) {
            attachURL = getAttachUrlFromMessage(attachmentCollection);
        }
        return attachURL;
    }

    public static String getSelectedFriendsFullNamesFromMap(List<User> usersList) {
        if (usersList.isEmpty()) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (User user : usersList) {
            stringBuilder.append(user.getFullName()).append(OCCUPANT_IDS_DIVIDER).append(" ");
        }

        return stringBuilder.toString().substring(ConstsCore.ZERO_INT_VALUE, stringBuilder.length() - 2);
    }

    public static ArrayList<Integer> createOccupantsIdsFromPrivateMessage(int currentUserId, int senderId) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<Integer>(2);
        occupantsIdsList.add(currentUserId);
        occupantsIdsList.add(senderId);
        return occupantsIdsList;
    }

    public static List<Integer> getOccupantsIdsListFromString(String occupantIds) {
        List<Integer> occupantIdsList = new ArrayList<Integer>();
        String[] occupantIdsArray = occupantIds.split(OCCUPANT_IDS_DIVIDER);
        for (String occupantId : occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId));
        }
        return occupantIdsList;
    }

    public static List<Integer> getOccupantIdsWithUser(List<Integer> friendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>(friendIdsList);
        occupantIdsList.add(user.getId());
        return occupantIdsList;
    }

    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, occupantIdsList);
    }

    public static QBDialog getExistPrivateDialog(DataManager dataManager, int opponentId) {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantForPrivateChat(opponentId);

        if (dialogOccupant != null) {
            Dialog dialog = dataManager.getDialogDataManager().getByDialogId(dialogOccupant.getDialog().getDialogId());
            return createQBDialogFromLocalDialog(dataManager, dialog);
        } else {
            return null;
        }
    }

    public static String getFullNameById(DataManager dataManager, int userId) {
        User user = dataManager.getUserDataManager().get(userId);

        if (user == null) {
            try {
                QBUser qbUser = QBUsers.getUser(userId);
                user = UserFriendUtils.createLocalUser(qbUser);
                dataManager.getUserDataManager().createOrUpdate(user);
            } catch (QBResponseException e) {
                ErrorUtils.logError(e);
            }
        }

        return user != null ? user.getFullName() : "";
    }

    public static String getFullNamesFromOpponentIds(DataManager dataManager, String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        return getFullNamesFromOpponentIdsList(dataManager, occupantsIdsList);
    }

    public static String getFullNamesFromOpponentId(DataManager dataManager, Integer userId,
            String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        occupantsIdsList.remove(userId);
        return getFullNamesFromOpponentIdsList(dataManager, occupantsIdsList);
    }

    private static String getFullNamesFromOpponentIdsList(DataManager dataManager, List<Integer> occupantsIdsList) {
        StringBuilder stringBuilder = new StringBuilder(occupantsIdsList.size());
        for (Integer id : occupantsIdsList) {
            stringBuilder.append(getFullNameById(dataManager, id)).append(OCCUPANT_IDS_DIVIDER).append(" ");
        }
        return stringBuilder.toString().substring(ConstsCore.ZERO_INT_VALUE, stringBuilder.length() - 2);
    }

    public static List<ParcelableQBDialog> qbDialogsToParcelableQBDialogs(List<QBDialog> dialogList){
        List<ParcelableQBDialog> parcelableDialogList = new ArrayList<ParcelableQBDialog>(dialogList.size());
        for (QBDialog dialog : dialogList) {
            ParcelableQBDialog parcelableQBDialog = new ParcelableQBDialog(dialog);
            parcelableDialogList.add(parcelableQBDialog);
        }
        return parcelableDialogList;
    }


    public static List<QBDialog> parcelableQBDialogsToQBDialogs(List<ParcelableQBDialog> parcelableQBDialogsList){
        List<QBDialog> qbDialogsList = new ArrayList<QBDialog>(parcelableQBDialogsList.size());
        for (ParcelableQBDialog parcelableQBDialog : parcelableQBDialogsList) {
            QBDialog qbDialog = parcelableQBDialog.getDialog();
            qbDialogsList.add(qbDialog);
        }
        return qbDialogsList;
    }

    public static User getOpponentFromPrivateDialog(User currentUser, List<DialogOccupant> occupantsList) {
        for (DialogOccupant dialogOccupant : occupantsList) {
            if (dialogOccupant != null && dialogOccupant.getUser() != null
                    && currentUser.getUserId() != dialogOccupant.getUser().getUserId()) {
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
        if (qbDialog.getUpdatedAt() != null) {
            dialog.setUpdatedAt(DateUtilsCore.getTime(qbDialog.getUpdatedAt()));
        }
        dialog.setModifiedDateLocal(qbDialog.getLastMessageDateSent());

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

    private static int getRandomUserFromOccupantsList(List<Integer> occupantsList) {
        for (Integer id : occupantsList) {
            if (!id.equals(AppSession.getSession().getUser().getId())) {
                return id;
            }
        }
        return 0;
    }

    public static List<Message> createTempLocalMessagesList(DataManager dataManager,
            List<QBDialog> qbDialogsList, QBDialog currentDialog) {
        List<Message> messagesList = new ArrayList<>();

        for (QBDialog qbDialog : qbDialogsList) {
            // dialog is opened
            Log.d("Fix double message", "currentDialog = " + currentDialog);
            if (currentDialog != null && qbDialog.getDialogId().equals(currentDialog.getDialogId())) {
                Log.d("Fix double message", "currentDialog = " + currentDialog + " currentDialogID = " + currentDialog.getDialogId());
                continue;
            }

            int randomUserId = getRandomUserFromOccupantsList(qbDialog.getOccupants());
            DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager()
                    .getDialogOccupant(qbDialog.getDialogId(), randomUserId);

            boolean onlyMeInDialog = qbDialog.getOccupants().size() == 1;

            if (dialogOccupant == null && onlyMeInDialog) {
                User user = dataManager.getUserDataManager().get(AppSession.getSession().getUser().getId());
                DbUtils.saveDialogOccupant(dataManager,
                        createDialogOccupant(dataManager, qbDialog.getDialogId(), user));
                dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(qbDialog.getDialogId(), AppSession.getSession().getUser().getId());
            }

            long tempMessageId = qbDialog.getDialogId().hashCode();
            Message message = createTempLocalMessage(tempMessageId, dialogOccupant, qbDialog.getLastMessage(), qbDialog.getLastMessageDateSent(), State.TEMP_LOCAL);
            messagesList.add(message);

            if (qbDialog.getUnreadMessageCount() != null && qbDialog.getUnreadMessageCount() > 0) {
                for (int i = 0; i < qbDialog.getUnreadMessageCount(); i++) {
                    messagesList.add(createTempLocalMessage(--tempMessageId, dialogOccupant, null, State.TEMP_LOCAL_UNREAD));
                }
            }
        }

        return messagesList;
    }

    public static Message createTempLocalMessage(long messageId, DialogOccupant dialogOccupant, String body, State state) {
        Message message = new Message();
        message.setMessageId(String.valueOf(messageId));
        message.setDialogOccupant(dialogOccupant);
        message.setState(state);
        message.setBody(body);
        return message;
    }

    public static Message createTempLocalMessage(DialogNotification dialogNotification) {
        Message message = new Message();
        message.setMessageId(dialogNotification.getDialogNotificationId());
        message.setDialogOccupant(dialogNotification.getDialogOccupant());
        message.setState(State.TEMP_LOCAL_UNREAD);
        message.setBody(dialogNotification.getBody());
        message.setCreatedDate(dialogNotification.getCreatedDate());
        return message;
    }

    private static Message createTempLocalMessage(long messageId, DialogOccupant dialogOccupant, String body, long createdDate, State state) {
        Message message = createTempLocalMessage(messageId, dialogOccupant, body, state);
        message.setCreatedDate(createdDate);
        return message;
    }

    public static List<QBDialog> createQBDialogsListFromDialogsList(DataManager dataManager, List<Dialog> dialogsList) {
        List<QBDialog> qbDialogsList = new ArrayList<>(dialogsList.size());

        for (Dialog dialog : dialogsList) {
            qbDialogsList.add(createQBDialogFromLocalDialog(dataManager, dialog));
        }

        return qbDialogsList;
    }

    public static List<DialogOccupant> createDialogOccupantsList(DataManager dataManager, QBDialog qbDialog, boolean onlyNewOccupant) {
        List<DialogOccupant> dialogOccupantsList = new ArrayList<>(qbDialog.getOccupants().size());

        for (Integer userId : qbDialog.getOccupants()) {
            DialogOccupant dialogOccupant;
            if (onlyNewOccupant) {
                dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(qbDialog.getDialogId(), userId);
                if (dialogOccupant == null) {
                    dialogOccupant = createDialogOccupant(dataManager, qbDialog.getDialogId(), dataManager.getUserDataManager().get(userId));
                } else {
                    dialogOccupant.setStatus(DialogOccupant.Status.ACTUAL);
                }
                dialogOccupantsList.add(dialogOccupant);
            } else {
                dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(qbDialog.getDialogId(), userId);
                if (dialogOccupant == null) {
                    User user = dataManager.getUserDataManager().get(userId);
                    if (user == null) {
                        user = QBRestHelper.loadAndSaveUser(userId);
                    }
                    dialogOccupant = createDialogOccupant(dataManager, qbDialog.getDialogId(), user);
                    dialogOccupantsList.add(dialogOccupant);
                }
            }
        }

        return dialogOccupantsList;
    }

    public static List<Long> getIdsFromDialogOccupantsList(List<DialogOccupant> dialogOccupantsList) {
        List<Long> idsList = new ArrayList<>(dialogOccupantsList.size());

        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            idsList.add(dialogOccupant.getDialogOccupantId());
        }

        return idsList;
    }

    public static List<Integer> getUsersIdsFromDialogOccupantsList(List<DialogOccupant> dialogOccupantsList) {
        List<Integer> idsList = new ArrayList<>(dialogOccupantsList.size());

        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            idsList.add(dialogOccupant.getUser().getUserId());
        }

        return idsList;
    }

    public static QBDialog createQBDialogFromLocalDialog(DataManager dataManager, Dialog dialog) {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        QBDialog qbDialog = createQBDialogFromLocalDialog(dialog, dialogOccupantsList);
        return qbDialog;
    }

    public static QBDialog createQBDialogFromLocalDialogWithoutLeaved(DataManager dataManager, Dialog dialog) {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getActualDialogOccupantsByDialog(dialog.getDialogId());
        QBDialog qbDialog = createQBDialogFromLocalDialog(dialog, dialogOccupantsList);
        return qbDialog;
    }

    private static QBDialog createQBDialogFromLocalDialog(Dialog dialog, List<DialogOccupant> dialogOccupantsList) {
        QBDialog qbDialog = new QBDialog();
        qbDialog.setDialogId(dialog.getDialogId());
        qbDialog.setRoomJid(dialog.getRoomJid());
        qbDialog.setPhoto(dialog.getPhoto());
        qbDialog.setName(dialog.getTitle());
        qbDialog.setOccupantsIds(createOccupantsIdsFromDialogOccupantsList(dialogOccupantsList));
        qbDialog.setType(
                Dialog.Type.PRIVATE.equals(dialog.getType()) ? QBDialogType.PRIVATE : QBDialogType.GROUP);
        qbDialog.setUpdatedAt(new Date(dialog.getUpdatedAt()));
        return qbDialog;
    }

    public static ArrayList<Integer> createOccupantsIdsFromDialogOccupantsList(
            List<DialogOccupant> dialogOccupantsList) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<>(dialogOccupantsList.size());
        for (DialogOccupant dialogOccupant : dialogOccupantsList) {
            occupantsIdsList.add(dialogOccupant.getUser().getUserId());
        }
        return occupantsIdsList;
    }

    public static ArrayList<Integer> createOccupantsIdsFromUsersList(
            List<User> usersList) {
        ArrayList<Integer> occupantsIdsList = new ArrayList<>(usersList.size());
        for (User user : usersList) {
            occupantsIdsList.add(user.getUserId());
        }
        return occupantsIdsList;
    }

    public static Message createLocalMessage(QBChatMessage qbChatMessage, DialogOccupant dialogOccupant, State state) {
        long dateSent = getMessageDateSent(qbChatMessage);
        Message message = new Message();
        message.setMessageId(qbChatMessage.getId());
        message.setDialogOccupant(dialogOccupant);
        message.setCreatedDate(dateSent);
        message.setState(qbChatMessage.isRead() ? State.READ : state);
        message.setBody(qbChatMessage.getBody());
        return message;
    }

    public static long getMessageDateSent(QBChatMessage qbChatMessage) {
        long dateSent;
        String dateSentString = (String) qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_DATE_SENT);
        try {
            dateSent = dateSentString != null ? Long.parseLong(dateSentString) : qbChatMessage.getDateSent();
        } catch (NumberFormatException e) {
            dateSent = DateUtilsCore.getCurrentTime();
        }
        return dateSent;
    }

    public static String getRoomJid(String dialogId) {
        return QBSettings.getInstance().getApplicationId()
                .concat("_")
                .concat(dialogId)
                .concat(ConstsCore.CHAT_MUC)
                .concat(QBSettings.getInstance().getChatServerDomain());
    }

    public static Attachment createLocalAttachment(QBAttachment qbAttachment) {
        Attachment attachment = new Attachment();
        attachment.setAttachmentId(qbAttachment.getId());
        attachment.setRemoteUrl(qbAttachment.getUrl());
        attachment.setName(qbAttachment.getName());
        attachment.setSize(qbAttachment.getSize());
        return attachment;
    }

    public static DialogNotification createLocalDialogNotification(Context context, DataManager dataManager, QBChatMessage qbChatMessage, DialogOccupant dialogOccupant) {
        DialogNotification dialogNotification = new DialogNotification();
        dialogNotification.setDialogNotificationId(qbChatMessage.getId());
        dialogNotification.setDialogOccupant(dialogOccupant);

        String chatNotificationTypeString = qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE).toString();

        if (chatNotificationTypeString != null) {

            int chatNotificationTypeInt = Integer.parseInt(chatNotificationTypeString);
            NotificationType chatNotificationType = NotificationType.parseByValue(chatNotificationTypeInt);
            DialogNotification.Type dialogNotificationTypeLocal = DialogNotification.Type
                    .parseByCode(chatNotificationTypeInt);

            switch (chatNotificationType) {
                case GROUP_CHAT_CREATE:
                case GROUP_CHAT_UPDATE:
                    dialogNotification.setType(
                            ChatNotificationUtils.getUpdateChatLocalNotificationType(qbChatMessage));
                    dialogNotification.setBody(ChatNotificationUtils
                            .getBodyForUpdateChatNotificationMessage(context, dataManager, qbChatMessage));
                    break;
                case FRIENDS_REQUEST:
                case FRIENDS_ACCEPT:
                case FRIENDS_REJECT:
                case FRIENDS_REMOVE:
                    dialogNotification.setType(dialogNotificationTypeLocal);
                    dialogNotification.setBody(ChatNotificationUtils
                            .getBodyForFriendsNotificationMessage(context, dataManager,
                                    dialogNotificationTypeLocal, qbChatMessage));
                    break;
            }
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
        combinationMessagesList.addAll(getCombinationMessagesListFromMessagesList(messagesList));
        combinationMessagesList.addAll(getCombinationMessagesListFromDialogNotificationsList(
                dialogNotificationsList));
        Collections.sort(combinationMessagesList, new CombinationMessage.DateComparator());
        return combinationMessagesList;
    }

    public static String getDialogLastMessage(String defaultLasMessage, Message message, DialogNotification dialogNotification) {
        String lastMessage = "";

        if (message == null && dialogNotification != null) {
            lastMessage = defaultLasMessage;
        } else if (dialogNotification == null && message != null) {
            lastMessage = message.getBody();
        } else if (message != null && dialogNotification != null) {
            lastMessage = message.getCreatedDate() > dialogNotification.getCreatedDate()
                    ? message.getBody() : defaultLasMessage;
        }

        return lastMessage;
    }

    public static long getDialogMessageCreatedDate(boolean lastMessage, Message message, DialogNotification dialogNotification) {
        long createdDate = 0;

        if (message == null && dialogNotification == null) {
            createdDate = 0;
        } else if (message != null && dialogNotification != null) {
            createdDate = lastMessage
                    ? (message.getCreatedDate() > dialogNotification.getCreatedDate() ? message.getCreatedDate() : dialogNotification.getCreatedDate())
                    : (message.getCreatedDate() < dialogNotification.getCreatedDate() ? message.getCreatedDate() : dialogNotification.getCreatedDate());
        } else if (message != null && dialogNotification == null) {
            createdDate = message.getCreatedDate();
        } else if (dialogNotification != null && message == null) {
            createdDate = dialogNotification.getCreatedDate();
        }

        return createdDate;
    }

    public static List<Dialog> fillTitleForPrivateDialogsList(String titleForDeletedUser, DataManager dataManager,
            List<Dialog> inputDialogsList) {
        List<Dialog> dialogsList = new ArrayList<>(inputDialogsList.size());

        for (Dialog dialog : inputDialogsList) {
            if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
                List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                        .getDialogOccupantsListByDialogId(dialog.getDialogId());
                User currentUser =  UserFriendUtils.createLocalUser(AppSession.getSession().getUser());
                User opponentUser = getOpponentFromPrivateDialog(currentUser, dialogOccupantsList);
                if (opponentUser.getFullName() != null) {
                    dialog.setTitle(opponentUser.getFullName());
                    dialogsList.add(dialog);
                } else {
                    dialog.setTitle(titleForDeletedUser);
                }
            } else {
                dialogsList.add(dialog);
            }
        }

        return dialogsList;
    }

    public static int getOnlineDialogOccupantsCount(QBFriendListHelper friendListHelper, List<Integer> occupantIdsList) {
        int onlineOccupantsCount = 0;

        for (int userId : occupantIdsList) {
            if (userId == AppSession.getSession().getUser().getId() || friendListHelper.isUserOnline(userId)) {
                onlineOccupantsCount++;
            }
        }

        return onlineOccupantsCount;
    }

    public static List<DialogOccupant> getUpdatedDialogOccupantsList(DataManager dataManager, String dialogId, List<Integer> dialogOccupantIdsList, DialogOccupant.Status status) {
        List<DialogOccupant> updatedDialogOccupantsList = new ArrayList<>(dialogOccupantIdsList.size());

        for (Integer userId : dialogOccupantIdsList) {
            User user = dataManager.getUserDataManager().get(userId);
            if (user == null) {
                user = QBRestHelper.loadAndSaveUser(userId);
            }

            DialogOccupant dialogOccupant = getUpdatedDialogOccupant(dataManager, dialogId, status, userId);
            if (dialogOccupant == null) {
                dialogOccupant = createDialogOccupant(dataManager, dialogId, user);
                dialogOccupant.setStatus(status);
            }

            updatedDialogOccupantsList.add(dialogOccupant);
        }

        return updatedDialogOccupantsList;
    }

    public static DialogOccupant getUpdatedDialogOccupant(DataManager dataManager, String dialogId,
            DialogOccupant.Status status, Integer userId) {
        DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, userId);
        if (dialogOccupant != null) {
            dialogOccupant.setStatus(status);
        }
        return dialogOccupant;
    }

    public static DialogOccupant createDialogOccupant(DataManager dataManager, String dialogId, User user) {
        DialogOccupant dialogOccupant = new DialogOccupant();
        dialogOccupant.setUser(user);
        dialogOccupant.setDialog(dataManager.getDialogDataManager().getByDialogId(dialogId));
        return dialogOccupant;
    }

    public static void addOccupantsToQBDialog(QBDialog qbDialog, QBChatMessage qbChatMessage) {
        qbDialog.setOccupantsIds(new ArrayList<Integer>(2));
        qbDialog.getOccupants().add(qbChatMessage.getSenderId());
        qbDialog.getOccupants().add(qbChatMessage.getRecipientId());
    }
}