package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.ChatNotificationType;
import com.quickblox.q_municate_core.models.NotificationType;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatNotificationUtils {

    public static final String PROPERTY_DIALOG_ID = "dialog_id";
    public static final String PROPERTY_ROOM_NAME = "room_name";
    public static final String PROPERTY_ROOM_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_ROOM_JID = "room_jid";
    public static final String PROPERTY_ROOM_PHOTO = "room_photo";
    public static final String PROPERTY_ROOM_UPDATED_AT = "room_updated_date";
    public static final String PROPERTY_ROOM_UPDATE_INFO = "dialog_update_info";

    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String PROPERTY_DATE_SENT = "date_sent";
    public static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    public static final String VALUE_SAVE_TO_HISTORY = "1";

    public static QBDialog parseDialogFromQBMessage(Context context, QBChatMessage qbChatMessage, QBDialogType qbDialogType) {
        String dialogId = (String) qbChatMessage.getProperty(PROPERTY_DIALOG_ID);
        String roomJid = (String) qbChatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = (String) qbChatMessage.getProperty(PROPERTY_ROOM_OCCUPANTS_IDS);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = (String) qbChatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        long dateSent = ChatUtils.getMessageDateSent(qbChatMessage);

        QBDialog qbDialog = new QBDialog(dialogId);
        qbDialog.setRoomJid(roomJid);
        qbDialog.setPhoto(photoUrl);
        qbDialog.setType(qbDialogType);

        qbDialog.setName(dialogName);

        if (!TextUtils.isEmpty(occupantsIds)) {
            qbDialog.setOccupantsIds((ArrayList<Integer>) ChatUtils.getOccupantsIdsListFromString(occupantsIds));
        }

        if (!qbChatMessage.getAttachments().isEmpty()) {
            qbDialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(qbChatMessage.getBody())) {
            qbDialog.setLastMessage(qbChatMessage.getBody());
        }

        qbDialog.setLastMessageDateSent(dateSent);
        qbDialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);

        return qbDialog;
    }

    public static QBDialog parseDialogFromQBMessage(Context context, QBChatMessage qbChatMessage,
            String lastMessage, QBDialogType qbDialogType) {
        QBDialog qbDialog = parseDialogFromQBMessage(context, qbChatMessage, qbDialogType);

        if (!qbChatMessage.getAttachments().isEmpty()) {
            qbDialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(lastMessage)) {
            qbDialog.setLastMessage(lastMessage);
        }

        return qbDialog;
    }

    public static void updateDialogFromQBMessage(Context context, DataManager dataManager, QBChatMessage qbChatMessage, QBDialog qbDialog) {
        String lastMessage = getBodyForUpdateChatNotificationMessage(context, dataManager, qbChatMessage);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = (String) qbChatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String occupantsList = (String) qbChatMessage.getProperty(PROPERTY_ROOM_OCCUPANTS_IDS);
        String updatedInfo = (String) qbChatMessage.getProperty(PROPERTY_ROOM_UPDATE_INFO);

        ChatNotificationType chatNotificationType = null;

        if (updatedInfo != null) {
            chatNotificationType = ChatNotificationType.parseByValue(Integer.parseInt(updatedInfo));
        }

        if (chatNotificationType != null) {
            switch (chatNotificationType) {
                case CHAT_PHOTO:
                    setDialogPhoto(qbDialog, photoUrl);
                    break;
                case CHAT_NAME:
                    setDialogName(qbDialog, dialogName);
                    break;
                case CHAT_OCCUPANTS:
                    setDialogOccupants(dataManager, qbDialog, occupantsList);
                    break;
            }
        }

        qbDialog.setLastMessage(lastMessage);
    }

    private static void setDialogPhoto(QBDialog qbDialog, String photoUrl) {
        if (!TextUtils.isEmpty(photoUrl)) {
            qbDialog.setPhoto(photoUrl);
        }
    }

    private static void setDialogName(QBDialog qbDialog, String dialogName) {
        if (!TextUtils.isEmpty(dialogName)) {
            qbDialog.setName(dialogName);
        }
    }

    private static void setDialogOccupants(DataManager dataManager, QBDialog qbDialog, String dialogOccupants) {
        if (!TextUtils.isEmpty(dialogOccupants)) {
            List<Integer> newOccupantsList = ChatUtils.getOccupantsIdsListFromString(dialogOccupants);
            if (newOccupantsList != null && !newOccupantsList.isEmpty()) {
                List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager()
                        .getDialogOccupantsListByDialogId(qbDialog.getDialogId());
                List<Integer> oldOccupantsList = ChatUtils.getUsersIdsFromDialogOccupantsList(
                        dialogOccupantsList);
                for (Integer occupantId : oldOccupantsList) {
                    DialogOccupant.Status status;
                    if (newOccupantsList.contains(occupantId)) {
                        qbDialog.getOccupants().add(occupantId);
                        status = DialogOccupant.Status.NORMAL;
                    } else {
                        qbDialog.getOccupants().remove(occupantId);
                        status = DialogOccupant.Status.LEAVED;
                    }
                    DbUtils.updateDialogOccupant(dataManager, qbDialog.getDialogId(), occupantId, status);
                }
            }
        }
    }

    public static QBChatMessage createSystemMessageAboutCreatingGroupChat(QBDialog dialog, String body) {
        String occupantsIds = ChatUtils.getOccupantsIdsStringFromList(dialog.getOccupants());

        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setBody(body);
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE,
                String.valueOf(NotificationType.GROUP_CHAT_CREATE.getValue()));
        qbChatMessage.setProperty(PROPERTY_ROOM_OCCUPANTS_IDS, occupantsIds);
        qbChatMessage.setProperty(PROPERTY_ROOM_JID, dialog.getRoomJid());
        qbChatMessage.setProperty(PROPERTY_ROOM_NAME, dialog.getName());

        if (dialog.getPhoto() != null) {
            qbChatMessage.setProperty(PROPERTY_ROOM_PHOTO, dialog.getPhoto());
        }

        return qbChatMessage;
    }

    public static boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null;
    }

    public static String getBodyForFriendsNotificationMessage(Context context, DataManager dataManager,
            DialogNotification.Type notificationType, QBChatMessage qbChatMessage) {
        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.frl_friends_contact_request);
        QBUser user = AppSession.getSession().getUser();
        int senderId;

        if (qbChatMessage.getSenderId() == null) {
            senderId = user.getId();
        } else {
            senderId = qbChatMessage.getSenderId();
        }

        boolean ownMessage = user.getId() == senderId;

        switch (notificationType) {
            case FRIENDS_REQUEST: {
                resultMessage = ownMessage
                        ? resources.getString(R.string.frl_friends_request_message_for_me)
                        : resources.getString(R.string.frl_friends_request_message_for_friend,
                        ChatUtils.getFullNameById(dataManager, senderId));
                break;
            }
            case FRIENDS_ACCEPT: {
                resultMessage = ownMessage
                        ? resources.getString(R.string.frl_friends_request_accept_message_for_me)
                        : resources.getString(R.string.frl_friends_request_accept_message_for_friend);
                break;
            }
            case FRIENDS_REJECT: {
                resultMessage = ownMessage
                        ? resources.getString(R.string.frl_friends_request_reject_message_for_me)
                        : resources.getString(R.string.frl_friends_request_reject_message_for_friend);
                break;
            }
            case FRIENDS_REMOVE: {
                User opponentUser;

                if (qbChatMessage.getRecipientId().intValue() == user.getId().intValue()) {
                    opponentUser = dataManager.getUserDataManager().get(senderId);
                    resultMessage =
                            resources.getString(R.string.frl_friends_request_remove_message_for_friend,
                            opponentUser.getFullName());
                } else {
                    opponentUser = dataManager.getUserDataManager().get(qbChatMessage.getRecipientId());
                    resultMessage = resources.getString(R.string.frl_friends_request_remove_message_for_me,
                            opponentUser.getFullName());
                }

                break;
            }
        }

        return resultMessage;
    }

    public static QBChatMessage createChatMessageForFriendsRequests(Context context, NotificationType notificationType) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setSenderId(AppSession.getSession().getUser().getId());
        qbChatMessage.setBody(context.getResources().getString(R.string.frl_friends_contact_request));
        qbChatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, String.valueOf(notificationType.getValue()));
        return qbChatMessage;
    }

    public static QBChatMessage createNotificationMessageForCreateGroupChat(Context context,
            Collection<Integer> addedFriendIdsList, String photoUrl) {
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE,
                String.valueOf(NotificationType.GROUP_CHAT_CREATE.getValue()));
        qbChatMessage.setBody(context.getResources().getString(R.string.cht_notification_message));
        qbChatMessage.setProperty(PROPERTY_ROOM_OCCUPANTS_IDS,
                ChatUtils.getOccupantsIdsStringFromList(addedFriendIdsList));
        if (photoUrl != null) {
            qbChatMessage.setProperty(PROPERTY_ROOM_PHOTO, photoUrl);
        }
        return qbChatMessage;
    }

    public static QBChatMessage createNotificationMessageForUpdateChat(Context context, QBDialog qbDialog,
            DialogNotification.Type notificationType, Collection<Integer> addedFriendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        QBChatMessage qbChatMessage = new QBChatMessage();
        qbChatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        qbChatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE,
                String.valueOf(NotificationType.GROUP_CHAT_UPDATE.getValue()));
        qbChatMessage.setProperty(PROPERTY_ROOM_UPDATED_AT, String.valueOf(qbDialog.getUpdatedAt().getTime()));
        qbChatMessage.setBody(context.getResources().getString(R.string.cht_notification_message));

        ChatNotificationType chatNotificationType = null;

        switch (notificationType) {
            case NAME_DIALOG: {
                qbChatMessage.setProperty(PROPERTY_ROOM_NAME, qbDialog.getName());
                chatNotificationType = ChatNotificationType.CHAT_NAME;
                break;
            }
            case PHOTO_DIALOG: {
                qbChatMessage.setProperty(PROPERTY_ROOM_PHOTO, qbDialog.getPhoto());
                chatNotificationType = ChatNotificationType.CHAT_PHOTO;
                break;
            }
            case OCCUPANTS_DIALOG: {
                qbChatMessage.setProperty(PROPERTY_ROOM_OCCUPANTS_IDS, user.getId() + ConstsCore.EMPTY_STRING);
                chatNotificationType = ChatNotificationType.CHAT_OCCUPANTS;
                break;
            }
            case CREATE_DIALOG: {
                qbChatMessage.setProperty(PROPERTY_ROOM_OCCUPANTS_IDS,
                        ChatUtils.getOccupantsIdsStringFromList(addedFriendIdsList));
                chatNotificationType = ChatNotificationType.CHAT_OCCUPANTS;
                break;
            }
            case ADDED_DIALOG: {
                qbChatMessage.setProperty(PROPERTY_ROOM_OCCUPANTS_IDS,
                        ChatUtils.getOccupantsIdsStringFromList(qbDialog.getOccupants()));
                chatNotificationType = ChatNotificationType.CHAT_OCCUPANTS;
                break;
            }
        }

        if (chatNotificationType != null) {
            qbChatMessage.setProperty(PROPERTY_ROOM_UPDATE_INFO, String.valueOf(chatNotificationType.getValue()));
        }

        return qbChatMessage;
    }

    public static DialogNotification.Type getUpdateChatNotificationMessageType(QBChatMessage qbChatMessage) {
        String notificationTypeString = (String) qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String updatedInfo = (String) qbChatMessage.getProperty(PROPERTY_ROOM_UPDATE_INFO);

        NotificationType notificationType = null;
        ChatNotificationType chatNotificationType = null;

        if (notificationTypeString != null) {
            notificationType = NotificationType.parseByValue(Integer.parseInt(notificationTypeString));
        }

        if (updatedInfo != null) {
            chatNotificationType = ChatNotificationType.parseByValue(Integer.parseInt(updatedInfo));
        }

        DialogNotification.Type dialogNotificationTypeLocal = null;

        if (chatNotificationType != null) {
            switch (chatNotificationType) {
                case CHAT_PHOTO:
                    dialogNotificationTypeLocal = DialogNotification.Type.PHOTO_DIALOG;
                    break;
                case CHAT_NAME:
                    dialogNotificationTypeLocal = DialogNotification.Type.NAME_DIALOG;
                    break;
                case CHAT_OCCUPANTS:
                    dialogNotificationTypeLocal = DialogNotification.Type.OCCUPANTS_DIALOG;
                    break;
            }
        }

        if (notificationType != null && chatNotificationType == null
                && notificationType.equals(NotificationType.GROUP_CHAT_CREATE)) {
            dialogNotificationTypeLocal = DialogNotification.Type.CREATE_DIALOG;
        }

        if (notificationType != null && notificationType.equals(NotificationType.GROUP_CHAT_UPDATE)) {
            dialogNotificationTypeLocal = DialogNotification.Type.ADDED_DIALOG;
        }

        return dialogNotificationTypeLocal;
    }

    public static String getBodyForUpdateChatNotificationMessage(Context context, DataManager dataManager,
            QBChatMessage qbChatMessage) {
        String notificationTypeString = (String) qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String updatedInfo = (String) qbChatMessage.getProperty(PROPERTY_ROOM_UPDATE_INFO);
        String inputOccupantsIds = (String) qbChatMessage.getProperty(PROPERTY_ROOM_OCCUPANTS_IDS);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);
        String dialogId = (String) qbChatMessage.getProperty(PROPERTY_DIALOG_ID);

        NotificationType notificationType = null;
        ChatNotificationType chatNotificationType = null;

        if (notificationTypeString != null) {
            notificationType = NotificationType.parseByValue(Integer.parseInt(notificationTypeString));
        }

        if (updatedInfo != null) {
            chatNotificationType = ChatNotificationType.parseByValue(Integer.parseInt(updatedInfo));
        }

        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.cht_notification_message);
        QBUser qbUser = AppSession.getSession().getUser();
        boolean ownMessage = qbUser.getId().equals(qbChatMessage.getSenderId());

        if (notificationType != null && notificationType.equals(NotificationType.GROUP_CHAT_CREATE)) {
            String fullNames;

            if (ownMessage) {
                fullNames = ChatUtils.getFullNamesFromOpponentId(dataManager, qbUser.getId(), inputOccupantsIds);
                resultMessage = resources.getString(R.string.cht_update_group_added_message, qbUser.getFullName(), fullNames);
            } else {
                fullNames = ChatUtils.getFullNamesFromOpponentId(dataManager, qbChatMessage.getSenderId(), inputOccupantsIds);
                resultMessage = resources.getString(R.string.cht_update_group_added_message, ChatUtils.getFullNameById(dataManager,
                        qbChatMessage.getSenderId()), fullNames);
            }

            return resultMessage;
        }

        if (chatNotificationType != null) {
            switch (chatNotificationType) {
                case CHAT_PHOTO:
                    resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_photo_message,
                            qbUser.getFullName()) : resources.getString(R.string.cht_update_group_photo_message,
                            ChatUtils.getFullNameById(dataManager, qbChatMessage.getSenderId()));
                    break;
                case CHAT_NAME:
                    resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_name_message,
                            qbUser.getFullName(), dialogName) : resources.getString(
                            R.string.cht_update_group_name_message, ChatUtils.getFullNameById(dataManager,
                                    qbChatMessage.getSenderId()), dialogName);
                    break;
                case CHAT_OCCUPANTS:
                    List<Integer> inputOccupantsList = ChatUtils.getOccupantsIdsListFromString(inputOccupantsIds);
                    if (inputOccupantsList != null && !inputOccupantsList.isEmpty()) {
                        String newOccupantIds = ChatUtils.getNewOccupantsIds(dataManager, dialogId, inputOccupantsList);

                        String fullNames = null;
                        if (newOccupantIds != null) {
                            fullNames = ChatUtils.getFullNamesFromOpponentIds(dataManager, newOccupantIds);
                        }

                        resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_added_message,
                                qbUser.getFullName(), fullNames) : resources.getString(
                                R.string.cht_update_group_added_message, ChatUtils.getFullNameById(dataManager,
                                        qbChatMessage.getSenderId()), fullNames);
                    }
                    break;
            }
        }

        return resultMessage;
    }
}