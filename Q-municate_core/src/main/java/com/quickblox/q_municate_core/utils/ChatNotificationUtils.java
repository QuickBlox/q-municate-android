package com.quickblox.q_municate_core.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBMessage;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.MessagesNotificationType;
import com.quickblox.users.model.QBUser;

import java.util.Collection;

public class ChatNotificationUtils {

    public static final int NOT_RESET_COUNTER = -1;

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_ROOM_NAME = "room_name";
    public static final String PROPERTY_ROOM_LEAVE = "deleted_id";
    public static final String PROPERTY_ROOM_PHOTO = "room_photo";
    public static final String PROPERTY_ROOM_JID = "room_jid";
    public static final String PROPERTY_DIALOG_ID = "dialog_id";
    public static final String PROPERTY_NOTIFICATION_TYPE = "notification_type";
    public static final String PROPERTY_MESSAGE_ID = "message_id";
    public static final String PROPERTY_DATE_SENT = "date_sent";
    public static final String PROPERTY_SAVE_TO_HISTORY = "save_to_history";

    public static final String VALUE_SAVE_TO_HISTORY = "1";

    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__GROUP_CHAT_CREATE = "1";

    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE = "2";
    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__ADDED_FRIENDS = "21";
    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__NAME = "22";
    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__PHOTO = "23";
    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__LEAVE = "24";
    public static final String PROPERTY_TYPE_TO_GROUP_CHAT__CREATE = "25";

    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REQUEST = "4";
    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_ACCEPT = "5";
    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REJECT = "6";
    public static final String PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REMOVE = "7";

    public static QBDialog parseDialogFromQBMessage(Context context, QBMessage chatMessage, QBDialogType dialogType) {
        String dialogId = chatMessage.getProperty(PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        long lastMessageDateSent = Long.parseLong(chatMessage.getProperty(PROPERTY_DATE_SENT));

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setPhoto(photoUrl);
        dialog.setName(dialogName);
        dialog.setType(dialogType);

        if (!TextUtils.isEmpty(occupantsIds)) {
            dialog.setOccupantsIds(ChatUtils.getOccupantsIdsListFromString(occupantsIds));
        }

        if (!chatMessage.getAttachments().isEmpty()) {
            dialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(chatMessage.getBody())) {
            dialog.setLastMessage(chatMessage.getBody());
        }

        dialog.setLastMessageDateSent(lastMessageDateSent);
        dialog.setUnreadMessageCount(ConstsCore.ZERO_INT_VALUE);

        return dialog;
    }

    public static QBDialog parseDialogFromQBMessage(Context context, QBMessage chatMessage,
            String lastMessage, QBDialogType dialogType) {
        QBDialog dialog = parseDialogFromQBMessage(context, chatMessage, dialogType);

        if (!chatMessage.getAttachments().isEmpty()) {
            dialog.setLastMessage(context.getString(R.string.dlg_attached_last_message));
        } else if (!TextUtils.isEmpty(lastMessage)) {
            dialog.setLastMessage(lastMessage);
        }

        return dialog;
    }

    public static QBChatMessage messageToPrivateChatAboutCreatingGroupChat(Context context, QBDialog dialog) {
        String occupantsIds = ChatUtils.getOccupantsIdsStringFromList(ChatUtils.getOccupantIdsWithoutUser(
                dialog.getOccupants()));

        QBUser user = AppSession.getSession().getUser();

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(R.string.user_created_room, user.getFullName()));
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_PRIVATE_CHAT__GROUP_CHAT_CREATE);
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);

        return chatMessage;
    }

    public static QBChatMessage createChatNotificationMessageForCreating(QBDialog dialog) {
        String occupantsIds = ChatUtils.getOccupantsIdsStringFromList(dialog.getOccupants());

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE);
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);

        return chatMessage;
    }

    public static QBChatMessage createUpdateChatNotificationMessage(QBDialog dialog) {
        String occupantsIds = ChatUtils.getOccupantsIdsStringFromList(dialog.getOccupants());
        String dialogName = dialog.getName();
        String photoUrl = dialog.getPhoto();

        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE);
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        if (!TextUtils.isEmpty(dialogName)) {
            chatMessage.setProperty(PROPERTY_ROOM_NAME, dialogName);
        }
        if (!TextUtils.isEmpty(photoUrl)) {
            chatMessage.setProperty(PROPERTY_ROOM_PHOTO, photoUrl);
        }

        return chatMessage;
    }

    public static int getNotificationTypeIfExist(QBChatMessage chatMessage) {
        int friendsMessageTypeCode = ConstsCore.ZERO_INT_VALUE;
        if (chatMessage.getProperty(ChatNotificationUtils.PROPERTY_NOTIFICATION_TYPE) != null) {
            friendsMessageTypeCode = Integer.parseInt(chatMessage.getProperty(
                    PROPERTY_NOTIFICATION_TYPE).toString());
        }
        return friendsMessageTypeCode;
    }

    public static QBChatMessage createNotificationMessageForFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REQUEST);
    }

    public static QBChatMessage createNotificationMessageForAcceptFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_ACCEPT);
    }

    public static QBChatMessage createNotificationMessageForRejectFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REJECT);
    }

    public static QBChatMessage createNotificationMessageForRemoveFriendsRequest(Context context) {
        return createChatMessageForFriendsRequests(context, R.string.frl_friends_contact_request,
                PROPERTY_TYPE_TO_PRIVATE_CHAT__FRIENDS_REMOVE);
    }

    private static QBChatMessage createChatMessageForFriendsRequests(Context context, int messageResourceId,
            String requestType) {
        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(messageResourceId, user.getFullName()));
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, requestType);
        return chatMessage;
    }

    public static QBChatMessage createNotificationMessageForUpdateDialog(Context context, QBDialog dialog,
            MessagesNotificationType messagesNotificationType, Collection<Integer> addedFriendIdsList) {
        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setProperty(PROPERTY_SAVE_TO_HISTORY, VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE);
        chatMessage.setBody(context.getResources().getString(R.string.cht_notification_message));

        switch (messagesNotificationType) {
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

    public static String getBodyForFriendsNotificationMessage(Context context,
            MessagesNotificationType messagesNotificationType, MessageCache messageCache) {
        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.cht_notification_message);
        QBUser user = AppSession.getSession().getUser();
        boolean ownMessage = user.getId().equals(messageCache.getSenderId());

        switch (messagesNotificationType) {
            case FRIENDS_REQUEST: {
                resultMessage = ownMessage ? resources.getString(
                        R.string.frl_friends_request_message_for_me) : resources.getString(
                        R.string.frl_friends_request_message_for_friend, ChatUtils.getFullNameById(context,
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
                        .getString(R.string.frl_friends_request_remove_message_for_friend,
                                ChatUtils.getFullNameById(context, messageCache.getSenderId()));
                break;
            }
        }

        return resultMessage;
    }

    public static MessagesNotificationType getNotificationMessageType(QBMessage chatMessage) {
        String notificationType = chatMessage.getProperty(PROPERTY_NOTIFICATION_TYPE);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leave = chatMessage.getProperty(PROPERTY_ROOM_LEAVE);

        if (!TextUtils.isEmpty(occupantsIds) && notificationType.equals(
                PROPERTY_TYPE_TO_GROUP_CHAT__GROUP_CHAT_UPDATE)) {
            return MessagesNotificationType.ADDED_DIALOG;
        }

        if (!TextUtils.isEmpty(occupantsIds) && notificationType.equals(
                PROPERTY_TYPE_TO_PRIVATE_CHAT__GROUP_CHAT_CREATE)) {
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

    public static String getBodyForChatNotificationMessage(Context context, QBMessage chatMessage) {
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_ROOM_PHOTO);
        String leave = chatMessage.getProperty(PROPERTY_ROOM_LEAVE);

        Resources resources = context.getResources();
        String resultMessage = resources.getString(R.string.cht_notification_message);
        QBUser user = AppSession.getSession().getUser();
        boolean ownMessage = user.getId().equals(chatMessage.getSenderId());

        if (!TextUtils.isEmpty(occupantsIds)) {
            String fullNames = ChatUtils.getFullNamesFromOpponentIds(context, occupantsIds);
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_added_message,
                    user.getFullName(), fullNames) : resources.getString(
                    R.string.cht_update_group_added_message, ChatUtils.getFullNameById(context,
                            chatMessage.getSenderId()), fullNames);
        }

        if (!TextUtils.isEmpty(dialogName)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_name_message,
                    user.getFullName(), dialogName) : resources.getString(
                    R.string.cht_update_group_name_message, ChatUtils.getFullNameById(context,
                            chatMessage.getSenderId()), dialogName);
        }

        if (!TextUtils.isEmpty(photoUrl)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_photo_message,
                    user.getFullName()) : resources.getString(R.string.cht_update_group_photo_message,
                    ChatUtils.getFullNameById(context, chatMessage.getSenderId()));
        }

        if (!TextUtils.isEmpty(leave)) {
            resultMessage = ownMessage ? resources.getString(R.string.cht_update_group_leave_message,
                    user.getFullName()) : resources.getString(R.string.cht_update_group_leave_message,
                    ChatUtils.getFullNameById(context, chatMessage.getSenderId()));
        }

        return resultMessage;
    }

    public static boolean isUpdateDialogNotificationMessage(int messagesNotificationTypeCode) {
        return PROPERTY_TYPE_TO_GROUP_CHAT__CREATE.equals(
                messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_GROUP_CHAT__ADDED_FRIENDS
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_GROUP_CHAT__NAME
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_GROUP_CHAT__PHOTO
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING) || PROPERTY_TYPE_TO_GROUP_CHAT__LEAVE
                .equals(messagesNotificationTypeCode + ConstsCore.EMPTY_STRING);
    }
}