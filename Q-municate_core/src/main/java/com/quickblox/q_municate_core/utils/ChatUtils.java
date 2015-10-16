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

    public static String createChatName(ArrayList<User> usersList) {
        String userFullName = AppSession.getSession().getUser().getFullName();

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(userFullName).append(OCCUPANT_IDS_DIVIDER).append(" ");

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
        dialog.setModifiedDate(qbDialog.getLastMessageDateSent());

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

    public static List<Message> createTempLocalMessagesList(Context context, DataManager dataManager, List<QBDialog> qbDialogsList) {
        List<Message> messagesList = new ArrayList<>();

        for (QBDialog qbDialog : qbDialogsList) {
            int randomUserId = getRandomUserFromOccupantsList(qbDialog.getOccupants());
            DialogOccupant dialogOccupant = dataManager.getDialogOccupantDataManager()
                    .getDialogOccupant(qbDialog.getDialogId(), randomUserId);

            if (dialogOccupant == null && qbDialog.getOccupants().size() == 1) {
                dialogOccupant = saveDialogOccupantIfUserNotExists(context, dataManager, qbDialog.getDialogId(), qbDialog.getLastMessageUserId());
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

    public static List<DialogOccupant> createDialogOccupantsList(DataManager dataManager, QBDialog qbDialog) {
        List<DialogOccupant> dialogOccupantsList = new ArrayList<>(qbDialog.getOccupants().size());

        for (Integer userId : qbDialog.getOccupants()) {
            DialogOccupant dialogOccupant = new DialogOccupant();
            dialogOccupant.setUser(dataManager.getUserDataManager().get(userId));
            dialogOccupant.setDialog(dataManager.getDialogDataManager().getByDialogId(qbDialog.getDialogId()));

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

    public static QBDialog createQBDialogFromLocalDialog(DataManager dataManager, Dialog dialog) {
        QBDialog qbDialog = new QBDialog();
        qbDialog.setDialogId(dialog.getDialogId());
        qbDialog.setRoomJid(dialog.getRoomJid());
        qbDialog.setPhoto(dialog.getPhoto());
        qbDialog.setName(dialog.getTitle());
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                .getDialogOccupantsListByDialogId(dialog.getDialogId());
        qbDialog.setOccupantsIds(createOccupantsIdsFromDialogOccupantsList(dialogOccupantsList));
        qbDialog.setType(
                Dialog.Type.PRIVATE.equals(dialog.getType()) ? QBDialogType.PRIVATE : QBDialogType.GROUP);
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
        message.setState(qbChatMessage.isRead() ? State.READ : state);
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

    public static DialogNotification createLocalDialogNotification(Context context, DataManager dataManager, QBChatMessage qbChatMessage, DialogOccupant dialogOccupant) {
        DialogNotification dialogNotification = new DialogNotification();
        dialogNotification.setDialogNotificationId(qbChatMessage.getId());
        dialogNotification.setDialogOccupant(dialogOccupant);

        int friendsMessageTypeCode = Integer.parseInt(qbChatMessage.getProperty(
                ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE).toString());
        if (ChatNotificationUtils.isFriendsNotificationMessage(friendsMessageTypeCode)) {
            dialogNotification.setType(DialogNotification.Type.parseByCode(friendsMessageTypeCode));
            dialogNotification.setBody(ChatNotificationUtils.getBodyForFriendsNotificationMessage(context,
                    dataManager, DialogNotification.Type.parseByCode(friendsMessageTypeCode), qbChatMessage));
        } else if (ChatNotificationUtils.PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE.equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING)
                || ChatNotificationUtils.PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE.equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING)) {
            dialogNotification.setBody(ChatNotificationUtils.getBodyForUpdateChatNotificationMessage(context, dataManager,
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

    public static DialogOccupant saveDialogOccupantIfUserNotExists(Context context, DataManager dataManager, String dialogId, int userId) {
        DialogOccupant dialogOccupant;
        User user = new QBRestHelper(context).loadUser(userId);
        dialogOccupant = new DialogOccupant();
        dialogOccupant.setUser(user);
        dialogOccupant.setDialog(dataManager.getDialogDataManager().getByDialogId(dialogId));
        dataManager.getDialogOccupantDataManager().createOrUpdate(dialogOccupant);
        return dialogOccupant;
    }

    public static void saveDialogToCache(DataManager dataManager, QBDialog qbDialog) {
        Dialog dialog = createLocalDialog(qbDialog);
        dataManager.getDialogDataManager().createOrUpdate(dialog);

        saveDialogsOccupants(dataManager, qbDialog);
    }

    public static void saveDialogsToCache(Context context, DataManager dataManager, List<QBDialog> qbDialogsList) {
        dataManager.getDialogDataManager().createOrUpdate(createLocalDialogsList(qbDialogsList));

        saveDialogsOccupants(dataManager, qbDialogsList);

        saveTempMessages(context, dataManager, qbDialogsList);
    }

    public static void saveTempMessages(Context context, DataManager dataManager, List<QBDialog> qbDialogsList) {
        dataManager.getMessageDataManager().createOrUpdate(
                createTempLocalMessagesList(context, dataManager, qbDialogsList));
    }

    public static void saveTempMessage(DataManager dataManager, Message message) {
        dataManager.getMessageDataManager().createOrUpdate(message);
        updateDialogModifiedDate(dataManager, message.getDialogOccupant().getDialog().getDialogId(),
                message.getCreatedDate(), false);
    }

    public static List<DialogOccupant> saveDialogsOccupants(DataManager dataManager, QBDialog qbDialog) {
        List<DialogOccupant> dialogOccupantsList = createDialogOccupantsList(dataManager, qbDialog);
        dataManager.getDialogOccupantDataManager().createOrUpdate(dialogOccupantsList);
        return dialogOccupantsList;
    }

    public static void saveDialogOccupant(DataManager dataManager, DialogOccupant dialogOccupant) {
        dataManager.getDialogOccupantDataManager().createOrUpdate(dialogOccupant);
    }

    public static void saveDialogsOccupants(DataManager dataManager, List<QBDialog> qbDialogsList) {
        for (QBDialog qbDialog : qbDialogsList) {
            saveDialogsOccupants(dataManager, qbDialog);
        }
    }

    public static void updateStatusMessageLocal(DataManager dataManager, Message message) {
        dataManager.getMessageDataManager().update(message, false);
    }

    public static void updateStatusNotificationMessageLocal(DataManager dataManager, DialogNotification dialogNotification) {
        dataManager.getDialogNotificationDataManager().update(dialogNotification, false);
    }

    public static void updateStatusMessageLocal(DataManager dataManager, String messageId, State state) {
        Message message = dataManager.getMessageDataManager().getByMessageId(messageId);
        if (message != null) {
            message.setState(state);
            dataManager.getMessageDataManager().update(message);
        }
    }

    public static void saveMessagesToCache(Context context, DataManager dataManager, List<QBChatMessage> qbMessagesList, String dialogId) {
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
            dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId,
                    AppSession.getSession().getUser().getId());
        } else {
            dialogOccupant = dataManager.getDialogOccupantDataManager().getDialogOccupant(dialogId, qbChatMessage.getSenderId());
        }

        if (dialogOccupant == null && qbChatMessage.getSenderId() != null) {
            dialogOccupant = saveDialogOccupantIfUserNotExists(context, dataManager, dialogId,
                    qbChatMessage.getSenderId());
        }

        boolean isDialogNotification = qbChatMessage.getProperty(ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE) != null;
        if (isDialogNotification) {
            saveDialogNotificationToCache(context, dataManager, dialogOccupant, qbChatMessage, notify);
        } else {
            Message message = createLocalMessage(qbChatMessage, dialogOccupant, state);
            if (qbChatMessage.getAttachments() != null && !qbChatMessage.getAttachments().isEmpty()) {
                ArrayList<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(qbChatMessage.getAttachments());
                Attachment attachment = createLocalAttachment(attachmentsList.get(0));
                message.setAttachment(attachment);

                dataManager.getAttachmentDataManager().createOrUpdate(attachment, notify);
            }

            dataManager.getMessageDataManager().createOrUpdate(message, notify);
        }
    }

    public static void updateDialogModifiedDate(DataManager dataManager, String dialogId, long modifiedDate, boolean notify) {
        Dialog dialog = dataManager.getDialogDataManager().getByDialogId(dialogId);
        updateDialogModifiedDate(dataManager, dialog, modifiedDate, notify);
    }

    private static void updateDialogModifiedDate(DataManager dataManager, String dialogId, boolean notify) {
        long modifiedDate = getDialogModifiedDate(dataManager, dialogId);
        updateDialogModifiedDate(dataManager, dialogId, modifiedDate, notify);
    }

    private static void updateDialogModifiedDate(DataManager dataManager, Dialog dialog, long modifiedDate, boolean notify) {
        if (dialog != null) {
            dialog.setModifiedDate(modifiedDate);
            dataManager.getDialogDataManager().update(dialog, notify);
        }
    }

    public static long getDialogModifiedDate(DataManager dataManager, String dialogId) {
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(dialogId);
        List<Integer> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);

        Message message = dataManager.getMessageDataManager().getLastMessageByDialogId(dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager().getLastDialogNotificationByDialogId(dialogOccupantsIdsList);

        return ChatUtils.getDialogMessageCreatedDate(true, message, dialogNotification);
    }

    public static void saveDialogNotificationToCache(Context context, DataManager dataManager, DialogOccupant dialogOccupant, QBChatMessage qbChatMessage, boolean notify) {
        DialogNotification dialogNotification = createLocalDialogNotification(context, dataManager, qbChatMessage, dialogOccupant);
        saveDialogNotificationToCache(dataManager, dialogNotification, notify);
    }

    private static void saveDialogNotificationToCache(DataManager dataManager, DialogNotification dialogNotification, boolean notify) {
        if (dialogNotification.getDialogOccupant() != null) {
            dataManager.getDialogNotificationDataManager().createOrUpdate(dialogNotification, notify);
        }
    }

    public static void deleteDialogLocal(DataManager dataManager, String dialogId) {
        dataManager.getDialogDataManager().deleteById(dialogId);
    }
}