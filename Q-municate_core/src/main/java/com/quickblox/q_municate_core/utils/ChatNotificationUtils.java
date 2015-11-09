package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatNotificationUtils {

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_ROOM_NAME = "room_name";
    public static final String PROPERTY_ROOM_LEAVE = "deleted_id";
    public static final String PROPERTY_ROOM_PHOTO = "room_photo";
    public static final String PROPERTY_ROOM_JID = "room_jid";
    public static final String PROPERTY_DIALOG_ID = "dialog_id";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String PROPERTY_DATE_SENT = "date_sent";
    public static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    public static final String VALUE_SAVE_TO_HISTORY = "1";

    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE = "1";
    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE = "2";

    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REQUEST = "4";
    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_ACCEPT = "5";
    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REJECT = "6";
    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REMOVE = "7";

    public static QBDialog parseDialogFromQBMessage(Context context, QBChatMessage qbChatMessage, QBDialogType qbDialogType) {
        String dialogId = (String) qbChatMessage.getProperty(PROPERTY_DIALOG_ID);
        String roomJid = (String) qbChatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = (String) qbChatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = (String) qbChatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        long dateSent = ChatUtils.getMessageDateSent(qbChatMessage);

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setPhoto(photoUrl);
        dialog.setType(qbDialogType);

        dialog.setName(dialogName);

        if (!TextUtils.isEmpty(occupantsIds)) {
            dialog.setOccupantsIds((ArrayList<Integer>) ChatUtils.getOccupantsIdsListFromString(occupantsIds));
        }

        if (!qbChatMessage.getAttachments().isEmpty()) {
            dialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(qbChatMessage.getBody())) {
            dialog.setLastMessage(qbChatMessage.getBody());
        }

        dialog.setLastMessageDateSent(dateSent);
        dialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);

        return dialog;
    }

    public static QBDialog parseDialogFromQBMessage(Context context, QBChatMessage qbChatMessage,
            String lastMessage, QBDialogType qbDialogType) {
        QBDialog dialog = parseDialogFromQBMessage(context, qbChatMessage, qbDialogType);

        if (!qbChatMessage.getAttachments().isEmpty()) {
            dialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(lastMessage)) {
            dialog.setLastMessage(lastMessage);
        }

        return dialog;
    }

    public static void updateDialogFromQBMessage(Context context, DataManager dataManager, QBChatMessage qbChatMessage, QBDialog qbDialog) {
        String lastMessage = getBodyForUpdateChatNotificationMessage(context, dataManager, qbChatMessage);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = (String) qbChatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leavedOccupants = (String) qbChatMessage.getProperty(PROPERTY_ROOM_LEAVE);
        String newOccupants = (String) qbChatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);

        qbDialog.setLastMessage(lastMessage);

        if (!TextUtils.isEmpty(dialogName)) {
            qbDialog.setName(dialogName);
        }

        if (!TextUtils.isEmpty(leavedOccupants)) {
            List<Integer> leavedOccupantsList = ChatUtils.getOccupantsIdsListFromString(leavedOccupants);
            qbDialog.getOccupants().removeAll(leavedOccupantsList);
            DbUtils.updateDialogOccupants(dataManager, qbDialog.getDialogId(), leavedOccupantsList, DialogOccupant.Status.LEAVED);
        }

        if (!TextUtils.isEmpty(newOccupants)) {
            List<Integer> newOccupantsList = ChatUtils.getOccupantsIdsListFromString(newOccupants);
            qbDialog.getOccupants().addAll(newOccupantsList);
            DbUtils.updateDialogOccupants(dataManager, qbDialog.getDialogId(), newOccupantsList, DialogOccupant.Status.NORMAL);
        }

        if (!TextUtils.isEmpty(photoUrl)) {
            qbDialog.setPhoto(photoUrl);
        }
    }

    public static QBChatMessage createMessageToPrivateChatAboutCreatingGroupChat(QBDialog dialog, String body) {
        String occupantsIds = ChatUtils.getOccupantsIdsStringFromList(dialog.getOccupants());

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(body);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE);
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        chatMessage.setProperty(PROPERTY_ROOM_JID, dialog.getRoomJid());
        chatMessage.setProperty(PROPERTY_ROOM_NAME, dialog.getName());
        chatMessage.setProperty(PROPERTY_ROOM_PHOTO, dialog.getPhoto());

        return chatMessage;
    }

    public static int getNotificationTypeIfExist(QBChatMessage chatMessage) {
        int friendsMessageTypeCode = ConstsCore.ZERO_INT_VALUE;
        if (chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null) {
            String inputCode = (String) chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
            if (PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE.equals(inputCode)) {
                friendsMessageTypeCode = DialogNotification.Type.CREATE_DIALOG.getCode();
            } else {
                friendsMessageTypeCode = Integer.parseInt(inputCode);
            }
        }
        return friendsMessageTypeCode;
    }

    public static boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null;
    }

    public static boolean isFriendsNotificationMessage(int friendsMessageTypeCode) {
        return PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REQUEST
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_ACCEPT
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REJECT
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REMOVE
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING);
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

    public static QBChatMessage createNotificationMessageForFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REQUEST);
    }

    public static QBChatMessage createNotificationMessageForAcceptFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_ACCEPT);
    }

    public static QBChatMessage createNotificationMessageForRejectFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REJECT);
    }

    public static QBChatMessage createNotificationMessageForRemoveFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REMOVE);
    }

    private static QBChatMessage createChatMessageForFriendsRequests(Context context, String requestType) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(R.string.frl_friends_contact_request));
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, requestType);
        return chatMessage;
    }

    public static QBChatMessage createNotificationMessageForCreateGroupChat(Context context,
            Collection<Integer> addedFriendIdsList, String photoUrl) {
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE);
        chatMessage.setBody(context.getResources().getString(R.string.cht_notification_message));
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, ChatUtils.getOccupantsIdsStringFromList(
                addedFriendIdsList));
        chatMessage.setProperty(PROPERTY_ROOM_PHOTO, photoUrl);
        return chatMessage;
    }

    public static QBChatMessage createNotificationMessageForUpdateChat(Context context, QBDialog dialog,
            DialogNotification.Type notificationType, Collection<Integer> addedFriendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE);
        chatMessage.setBody(context.getResources().getString(R.string.cht_notification_message));

        switch (notificationType) {
            case ADDED_DIALOG: {
                chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, ChatUtils.getOccupantsIdsStringFromList(
                        addedFriendIdsList));
                break;
            }
            case NAME_DIALOG: {
                chatMessage.setProperty(PROPERTY_ROOM_NAME, dialog.getName());
                break;
            }
            case PHOTO_DIALOG: {
                chatMessage.setProperty(PROPERTY_ROOM_PHOTO, dialog.getPhoto());
                break;
            }
            case LEAVE_DIALOG: {
                chatMessage.setProperty(PROPERTY_ROOM_LEAVE, user.getId() + ConstsCore.EMPTY_STRING);
                break;
            }
            case CREATE_DIALOG: {
                chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, ChatUtils.getOccupantsIdsStringFromList(
                        addedFriendIdsList));
                break;
            }
        }

        return chatMessage;
    }

    public static DialogNotification.Type getUpdateChatNotificationMessageType(
            QBChatMessage chatMessage) {
        String notificationType = (String) chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String occupantsIds = (String) chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = (String) chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = (String) chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leave = (String) chatMessage.getProperty(PROPERTY_ROOM_LEAVE);

        if (!TextUtils.isEmpty(occupantsIds) && notificationType.equals(
                PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE)) {
            return DialogNotification.Type.ADDED_DIALOG;
        }

        if (!TextUtils.isEmpty(occupantsIds) && notificationType.equals(
                PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE)) {
            return DialogNotification.Type.CREATE_DIALOG;
        }

        if (!TextUtils.isEmpty(dialogName)) {
            return DialogNotification.Type.NAME_DIALOG;
        }

        if (!TextUtils.isEmpty(photoUrl)) {
            return DialogNotification.Type.PHOTO_DIALOG;
        }

        if (!TextUtils.isEmpty(leave)) {
            return DialogNotification.Type.LEAVE_DIALOG;
        }

        return null;
    }

    public static String getBodyForUpdateChatNotificationMessage(Context context, DataManager dataManager,
    QBChatMessage qbChatMessage) {
        String occupantsIds = (String) qbChatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = (String) qbChatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = (String) qbChatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leave = (String) qbChatMessage.getProperty(PROPERTY_ROOM_LEAVE);
        String notificationType = (String) qbChatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);

        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.cht_notification_message);
        QBUser user = AppSession.getSession().getUser();
        boolean ownMessage = user.getId().equals(qbChatMessage.getSenderId());

        if (notificationType.equals(PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_CREATE)) {
            String fullNames;

            if (ownMessage) {
                fullNames = ChatUtils.getFullNamesFromOpponentId(dataManager, user.getId(), occupantsIds);
                resultMessage = resources.getString(R.string.cht_update_group_added_message, user.getFullName(), fullNames);
            } else {
                fullNames = ChatUtils.getFullNamesFromOpponentId(dataManager, qbChatMessage.getSenderId(), occupantsIds);
                resultMessage = resources.getString(R.string.cht_update_group_added_message, ChatUtils.getFullNameById(dataManager,
                        qbChatMessage.getSenderId()), fullNames);
            }

            return resultMessage;
        }

        if (!TextUtils.isEmpty(occupantsIds)) {
            String fullNames = ChatUtils.getFullNamesFromOpponentIds(dataManager, occupantsIds);
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_added_message,
                    user.getFullName(), fullNames) : resources.getString(
                    R.string.cht_update_group_added_message, ChatUtils.getFullNameById(dataManager,
                            qbChatMessage.getSenderId()), fullNames);
        }

        if (!TextUtils.isEmpty(dialogName)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_name_message,
                    user.getFullName(), dialogName) : resources.getString(
                    R.string.cht_update_group_name_message, ChatUtils.getFullNameById(dataManager,
                            qbChatMessage.getSenderId()), dialogName);
        }

        if (!TextUtils.isEmpty(photoUrl)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_photo_message,
                    user.getFullName()) : resources.getString(R.string.cht_update_group_photo_message,
                    ChatUtils.getFullNameById(dataManager, qbChatMessage.getSenderId()));
        }

        if (!TextUtils.isEmpty(leave)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_leave_message,
                    user.getFullName()) : resources.getString(R.string.cht_update_group_leave_message,
                    ChatUtils.getFullNameById(dataManager, qbChatMessage.getSenderId()));
        }

        return resultMessage;
    }
}