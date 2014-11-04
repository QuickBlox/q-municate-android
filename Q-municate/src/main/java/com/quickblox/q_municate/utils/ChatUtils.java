package com.quickblox.q_municate.utils;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.chat.model.QBMessage;
import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.service.QBServiceConsts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ChatUtils {

    public static final String OCCUPANT_IDS_DIVIDER = ",";
    public static final int NOT_RESET_COUNTER = -1;

    public static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    public static final String PROPERTY_ROOM_NAME = "name";
    public static final String PROPERTY_PHOTO_URL = "photo";
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

    public static int getOccupantIdFromList(ArrayList<Integer> occupantsIdsList) {
        QBUser user = AppSession.getSession().getUser();
        int resultId = Consts.ZERO_INT_VALUE;
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
        return Consts.EMPTY_STRING;
    }

    public static QBDialog parseDialogFromMessageForUpdate(Context context, QBMessage chatMessage, long dateSent) {
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_PHOTO_URL);

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

    public static QBDialog parseDialogFromMessage(Context context, QBMessage chatMessage, String lastMessage, long dateSent) {
        String dialogId = chatMessage.getProperty(ChatUtils.PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String photoUrl = chatMessage.getProperty(PROPERTY_PHOTO_URL);
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
        dialog.setUnreadMessageCount(Consts.ZERO_INT_VALUE);
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

    public static QBChatMessage createRoomNotificationMessage(Context context, QBDialog dialog) {
        return createChatNotificationMessage(context, dialog);
    }

    public static QBChatMessage createChatNotificationMessage(Context context, QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = getOccupantsIdsStringFromList(dialog.getOccupants());
        String dialogName = dialog.getName();
        String photoUrl = dialog.getPhoto();
        String dialogTypeCode = String.valueOf(dialog.getType().getCode());

        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(R.string.user_created_room, user.getFullName()));
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, PROPERTY_NOTIFICATION_TYPE_CREATE_CHAT);
        chatMessage.setProperty(PROPERTY_DIALOG_ID, dialogId);
        if (!TextUtils.isEmpty(roomJid)) {
            chatMessage.setProperty(PROPERTY_ROOM_JID, roomJid);
        }
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        if (!TextUtils.isEmpty(photoUrl)) {
            chatMessage.setProperty(PROPERTY_PHOTO_URL, photoUrl);
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
            chatMessage.setProperty(PROPERTY_PHOTO_URL, photoUrl);
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

    private static QBChatMessage createChatMessageForFriendsRequests(Context context, int messageResourceId, String requestType) {
        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(messageResourceId, user.getFullName()));
        chatMessage.setProperty(ChatUtils.PROPERTY_SAVE_TO_HISTORY, ChatUtils.VALUE_SAVE_TO_HISTORY);
        chatMessage.setProperty(PROPERTY_NOTIFICATION_TYPE, requestType);
        long time = DateUtils.getCurrentTime();
        chatMessage.setProperty(PROPERTY_DATE_SENT, time + Consts.EMPTY_STRING);
        return chatMessage;
    }

    public static String getOccupantsIdsStringFromList(List<Integer> occupantIdsList) {
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

    public static ArrayList<Integer> getFriendIdsList(List<User> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (User friend : friendList) {
            friendIdsList.add(friend.getUserId());
        }
        return friendIdsList;
    }

    public static String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = Consts.EMPTY_STRING;
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
                friendsMessageTypeCode + Consts.EMPTY_STRING) || ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_ACCEPT_REQUEST
                .equals(friendsMessageTypeCode + Consts.EMPTY_STRING) || ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REJECT_REQUEST
                .equals(friendsMessageTypeCode + Consts.EMPTY_STRING) || ChatUtils.PROPERTY_NOTIFICATION_TYPE_FRIENDS_REMOVE_REQUEST
                .equals(friendsMessageTypeCode + Consts.EMPTY_STRING);
    }
}