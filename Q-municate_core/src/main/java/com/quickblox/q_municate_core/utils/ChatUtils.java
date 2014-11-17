package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBMessage;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.db.DatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatUtils {

    public static final String OCCUPANT_IDS_DIVIDER = ",";
    public static final int NOT_RESET_COUNTER = -1;

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_ROOM_NAME = "room_name";
    public static final String PROPERTY_ROOM_LEAVE = "deleted_id";
    public static final String PROPERTY_ROOM_PHOTO = "room_photo";
    public static final String PROPERTY_DIALOG_TYPE_CODE = "type";
    public static final String PROPERTY_ROOM_JID = "room_jid";
    public static final String PROPERTY_DIALOG_ID = "dialog_id";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String PROPERTY_MESSAGE_ID = "message_id";
    public static final String PROPERTY_DATE_SENT = "date_sent";
    public static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";
    public static final String VALUE_SAVE_TO_HISTORY = "1";

    public static final String PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT = "1";
    public static final String PROPERTY_NOTIFICATION_TYPE_UPDATE_CHAT = "2";

    public static final String PROPERTY_NOTIFICATION_TYPE_FRIENDS_REQUEST = "4";
    public static final String PROPERTY_NOTIFICATION_TYPE_FRIENDS_ACCEPT_REQUEST = "5";
    public static final String PROPERTY_NOTIFICATION_TYPE_FRIENDS_REJECT_REQUEST = "6";
    public static final String PROPERTY_NOTIFICATION_TYPE_FRIENDS_REMOVE_REQUEST = "7";

    public static final String PROPERTY_NOTIFICATION_TYPE_CREATE_DIALOG_REQUEST = "8";
    public static final String PROPERTY_NOTIFICATION_TYPE_ADDED_DIALOG_REQUEST = "9";
    public static final String PROPERTY_NOTIFICATION_TYPE_NAME_DIALOG_REQUEST = "10";
    public static final String PROPERTY_NOTIFICATION_TYPE_PHOTO_DIALOG_REQUEST = "11";
    public static final String PROPERTY_NOTIFICATION_TYPE_LEAVE_DIALOG_REQUEST = "12";

    public static int getOccupantIdFromList(ArrayList<Integer> occupantsIdsList) {
        QBUser user = AppSession.getSession().getUser();
        int resultId = ConstsCore.ZERO_INT_VALUE;
        for (Integer id : occupantsIdsList) {
            if (!id.equals(user.getId())) {
                resultId = id;
                break;
            }
        }
        return resultId;
    }

    public static String getAttachUrlFromMessage(ArrayList<QBAttachment> attachmentsList) {
        if (!attachmentsList.isEmpty()) {
            return attachmentsList.get(attachmentsList.size() - 1).getUrl();
        }
        return ConstsCore.EMPTY_STRING;
    }

    public static QBDialog parseDialogFromMessageForUpdate(Context context, QBMessage chatMessage,
            long dateSent) {
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setPhoto(photoUrl);
        dialog.setOccupantsIds(getOccupantsIdsListFromString(occupantsIds));
        dialog.setName(dialogName);

        if (!chatMessage.getAttachments().isEmpty()) {
            dialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(chatMessage.getBody())) {
            dialog.setLastMessage(chatMessage.getBody());
        }

        dialog.setLastMessageDateSent(dateSent);
        dialog.setUnreadMessageCount(NOT_RESET_COUNTER);

        return dialog;
    }

    public static QBDialog parseDialogFromMessage(Context context, QBMessage chatMessage, String lastMessage,
            long dateSent) {
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String dialogTypeCode = chatMessage.getProperty(PROPERTY_DIALOG_TYPE_CODE);

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setPhoto(photoUrl);
        dialog.setOccupantsIds(getOccupantsIdsListFromString(occupantsIds));
        dialog.setName(dialogName);
        if (dialogTypeCode != null) {
            QBDialogType dialogType = parseDialogType(dialogTypeCode);
            if (dialogType != null) {
                dialog.setType(dialogType);
            }
        }

        if (!chatMessage.getAttachments().isEmpty()) {
            dialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(lastMessage)) {
            dialog.setLastMessage(lastMessage);
        }

        dialog.setLastMessageDateSent(dateSent);
        dialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);
        return dialog;
    }

    public static QBDialogType parseDialogType(String dialogTypeCode) {
        QBDialogType dialogType = null;
        try {
            int dialogCode = Integer.parseInt(dialogTypeCode);
            dialogType = QBDialogType.parseByCode(dialogCode);
        } catch (NumberFormatException e) {
            ErrorUtils.logError(e);
        }
        return dialogType;
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

    public static ArrayList<Integer> getOccupantIdsWithoutUser(Collection<Integer> friendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>(friendIdsList);
        occupantIdsList.remove(user.getId());
        return occupantIdsList;
    }

    public static QBChatMessage createRoomNotificationMessage(Context context, QBDialog dialog) {
        return createChatNotificationMessageToPrivateChat(context, dialog);
    }

    public static QBChatMessage createChatNotificationMessageToPrivateChat(Context context, QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = getOccupantsIdsStringFromList(getOccupantIdsWithoutUser(dialog.getOccupants()));
        String dialogName = dialog.getName();
        String photoUrl = dialog.getPhoto();
        String dialogTypeCode = String.valueOf(dialog.getType().getCode());

        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(R.string.user_created_room, user.getFullName()));
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT);
        chatMessage.setProperty(PROPERTY_DIALOG_ID, dialogId);
        if (!TextUtils.isEmpty(roomJid)) {
            chatMessage.setProperty(PROPERTY_ROOM_JID, roomJid);
        }
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        if (!TextUtils.isEmpty(photoUrl)) {
            chatMessage.setProperty(PROPERTY_ROOM_PHOTO, photoUrl);
        }
        if (!TextUtils.isEmpty(dialogName)) {
            chatMessage.setProperty(PROPERTY_ROOM_NAME, dialogName);
        }
        chatMessage.setProperty(PROPERTY_DIALOG_TYPE_CODE, dialogTypeCode);
        return chatMessage;
    }

    public static QBChatMessage createUpdateChatNotificationMessage(QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String occupantsIds = getOccupantsIdsStringFromList(dialog.getOccupants());
        String dialogName = dialog.getName();
        String photoUrl = dialog.getPhoto();

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_NOTIFICATION_TYPE_UPDATE_CHAT);
        chatMessage.setProperty(PROPERTY_DIALOG_ID, dialogId);
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        if (!TextUtils.isEmpty(dialogName)) {
            chatMessage.setProperty(PROPERTY_ROOM_NAME, dialogName);
        }
        if (!TextUtils.isEmpty(photoUrl)) {
            chatMessage.setProperty(PROPERTY_ROOM_PHOTO, photoUrl);
        }
        return chatMessage;
    }

    public static QBChatMessage createNotificationMessageForFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_NOTIFICATION_TYPE_FRIENDS_REQUEST);
    }

    public static QBChatMessage createNotificationMessageForAcceptFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_NOTIFICATION_TYPE_FRIENDS_ACCEPT_REQUEST);
    }

    public static QBChatMessage createNotificationMessageForRejectFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_NOTIFICATION_TYPE_FRIENDS_REJECT_REQUEST);
    }

    public static QBChatMessage createNotificationMessageForRemoveFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_NOTIFICATION_TYPE_FRIENDS_REMOVE_REQUEST);
    }

    private static QBChatMessage createChatMessageForFriendsRequests(Context context, int messageResourceId,
            String requestType) {
        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(messageResourceId, user.getFullName()));
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, requestType);
        long time = DateUtilsCore.getCurrentTime();
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
        return chatMessage;
    }

    public static QBChatMessage createNotificationMessageForUpdateDialog(Context context, QBDialog dialog,
            MessagesNotificationType messagesNotificationType, Collection<Integer> addedFriendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        long time = DateUtilsCore.getCurrentTime();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_DIALOG_ID, dialog.getDialogId());
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + ConstsCore.EMPTY_STRING);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_NOTIFICATION_TYPE_UPDATE_CHAT);
        chatMessage.setBody(context.getResources().getString(R.string.notification_message));

        switch (messagesNotificationType) {
            case ADDED_DIALOG: {
                chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, getOccupantsIdsStringFromList(
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
                chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, getOccupantsIdsStringFromList(
                        addedFriendIdsList));
                break;
            }
        }

        return chatMessage;
    }

    public static String getOccupantsIdsStringFromList(Collection<Integer> occupantIdsList) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, occupantIdsList);
    }

    public static boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE) != null;
    }

    public static String[] getOccupantsIdsArrayFromList(ArrayList<Integer> occupantsList) {
        String[] occupantsArray = new String[occupantsList.size()];
        for (int i = 0; i < occupantsList.size(); i++) {
            occupantsArray[i] = String.valueOf(occupantsList.get(i));
        }
        return occupantsArray;
    }

    public static ArrayList<Integer> getOccupantsIdsListForCreatePrivateDialog(int opponentId) {
        QBUser user = AppSession.getSession().getUser();
        ArrayList<Integer> occupantsIdsList = new ArrayList<Integer>();
        occupantsIdsList.add(user.getId());
        occupantsIdsList.add(opponentId);
        return occupantsIdsList;
    }

    public static String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = ConstsCore.EMPTY_STRING;
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        if (attachments != null && attachments.size() > 0) {
            attachURL = getAttachUrlFromMessage(new ArrayList<QBAttachment>(attachments));
        }
        return attachURL;
    }

    public static List<String> getRoomJidListFromDialogs(List<QBDialog> dialogsList) {
        List<String> roomJidList = new ArrayList<String>();
        for (QBDialog dialog : dialogsList) {
            if (dialog.getType() != QBDialogType.PRIVATE) {
                roomJidList.add(dialog.getRoomJid());
            }
        }
        return roomJidList;
    }

    public static Bundle getBundleForCreatePrivateChat(int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(QBServiceConsts.EXTRA_OPPONENT_ID, userId);
        return bundle;
    }

    public static QBDialog getExistPrivateDialog(Context context, int opponentId) {
        List<QBDialog> dialogList = DatabaseManager.getDialogsByOpponent(context, opponentId,
                QBDialogType.PRIVATE);
        if (!dialogList.isEmpty()) {
            return dialogList.get(0);
        } else {
            return null;
        }
    }

    public static QBDialog getExistDialogById(Context context, String dialogId) {
        return DatabaseManager.getDialogByDialogId(context, dialogId);
    }

    public static boolean isFriendsMessageTypeCode(int friendsMessageTypeCode) {
        return ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REQUEST.equals(
                friendsMessageTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_FRIENDS_ACCEPT_REQUEST
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_FRIENDS_REJECT_REQUEST
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_FRIENDS_REMOVE_REQUEST
                .equals(friendsMessageTypeCode + ConstsCore.EMPTY_STRING);
    }

    public static String getResourceBodyForNotificationType(Context context,
            MessagesNotificationType messagesNotificationType, MessageCache messageCache) {
        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.notification_message);
        QBUser user = AppSession.getSession().getUser();
        boolean ownMessage = user.getId().equals(messageCache.getSenderId());

        switch (messagesNotificationType) {
            case FRIENDS_REQUEST: {
                resultMessage = ownMessage ? resources.getString(
                        R.string.frl_friends_request_message_for_me) : resources.getString(
                        R.string.frl_friends_request_message_for_friend, getFullNameById(context,
                        messageCache.getSenderId()));
                break;
            }
            case FRIENDS_ACCEPT: {
                resultMessage = ownMessage ? resources.getString(
                        R.string.frl_friends_request_accept_message_for_me) : resources.getString(
                        R.string.frl_friends_request_accept_message_for_friend);
                break;
            }
            case FRIENDS_REJECT: {
                resultMessage = ownMessage ? resources.getString(
                        R.string.frl_friends_request_reject_message_for_me) : resources.getString(
                        R.string.frl_friends_request_reject_message_for_friend);
                break;
            }
            case FRIENDS_REMOVE: {
                resultMessage = ownMessage ? resources.getString(
                        R.string.frl_friends_request_remove_message_for_me, user.getFullName()) : resources
                        .getString(R.string.frl_friends_request_remove_message_for_friend, getFullNameById(
                                context, messageCache.getSenderId()));
                break;
            }
        }

        return resultMessage;
    }

    private static String getFullNameById(Context context, int userId) {
        return DatabaseManager.getUserById(context, userId).getFullName();
    }

    public static MessagesNotificationType getNotificationMessageType(QBMessage chatMessage) {
        String notificationType = chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leave = chatMessage.getProperty(PROPERTY_ROOM_LEAVE);

        if (!TextUtils.isEmpty(occupantsIds)
                && notificationType.equals(PROPERTY_NOTIFICATION_TYPE_UPDATE_CHAT)) {
            return MessagesNotificationType.ADDED_DIALOG;
        }

        if (!TextUtils.isEmpty(occupantsIds)
                && notificationType.equals(PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT)) {
            return MessagesNotificationType.CREATE_DIALOG;
        }

        if (!TextUtils.isEmpty(dialogName)) {
            return MessagesNotificationType.NAME_DIALOG;
        }

        if (!TextUtils.isEmpty(photoUrl)) {
            return MessagesNotificationType.PHOTO_DIALOG;
        }

        if (!TextUtils.isEmpty(leave)) {
            return MessagesNotificationType.LEAVE_DIALOG;
        }

        return null;
    }

    public static String getNotificationMessage(Context context, QBMessage chatMessage) {
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leave = chatMessage.getProperty(PROPERTY_ROOM_LEAVE);

        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.notification_message);
        QBUser user = AppSession.getSession().getUser();
        boolean ownMessage = user.getId().equals(chatMessage.getSenderId());

        if (!TextUtils.isEmpty(occupantsIds)) {
            String fullNames = getFullNamesFromOpponentIds(context, occupantsIds);
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_added_message, user.getFullName(), fullNames)
                    : resources.getString(R.string.cht_update_group_added_message, getFullNameById(context, chatMessage.getSenderId()), fullNames);
        }

        if (!TextUtils.isEmpty(dialogName)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_name_message, user.getFullName(), dialogName)
                    : resources.getString(R.string.cht_update_group_name_message, getFullNameById(context, chatMessage.getSenderId()), dialogName);
        }

        if (!TextUtils.isEmpty(photoUrl)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_photo_message, user.getFullName())
                    : resources.getString(R.string.cht_update_group_photo_message, getFullNameById(context,
                    chatMessage.getSenderId()));
        }

        if (!TextUtils.isEmpty(leave)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_leave_message, user.getFullName())
                    : resources.getString(R.string.cht_update_group_leave_message, getFullNameById(context, chatMessage.getSenderId()));
        }

        return resultMessage;
    }

    private static String getFullNamesFromOpponentIds(Context context, String occupantsIdsString) {
        List<Integer> occupantsIdsList = getOccupantsIdsListFromString(occupantsIdsString);
        StringBuilder stringBuilder = new StringBuilder(occupantsIdsList.size());
        for (Integer id : occupantsIdsList) {
            stringBuilder.append(getFullNameById(context, id)).append(OCCUPANT_IDS_DIVIDER);
        }
        return stringBuilder.toString().substring(ConstsCore.ZERO_INT_VALUE, stringBuilder.length() - 1);
    }

    public static boolean isNotificationMessageUpdateDialog(int messagesNotificationTypeCode) {
        return ChatUtils.PROPERTY_NOTIFICATION_TYPE_CREATE_DIALOG_REQUEST.equals(
                messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_ADDED_DIALOG_REQUEST
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_NAME_DIALOG_REQUEST
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_PHOTO_DIALOG_REQUEST
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_NOTIFICATION_TYPE_LEAVE_DIALOG_REQUEST
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING);
    }
}