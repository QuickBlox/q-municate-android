package com.quickblox.qmunicate.utils;

import android.text.TextUtils;

import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;

import java.util.ArrayList;
import java.util.List;

public class ChatUtils {

    private static final String OCCUPANT_IDS_DIVIDER = ",";

    private static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    private static final String PROPERTY_ROOM_NAME = "name";
    private static final String PROPERTY_DIALOG_TYPE_CODE = "type";
    private static final String PROPERTY_ROOM_JID = "room_jid";
    private static final String PROPERTY_DIALOG_ID = "_id";

    public static String getPrivateDialogIdByOccupantId(List<QBDialog> chatsDialogsList, int occupantId) {
        for (QBDialog dialog : chatsDialogsList) {
            List<Integer> occupantsIdsList = ChatUtils.getOccupantsIdsFromDialog(dialog);
            if (occupantsIdsList.contains(occupantId)) {
                return dialog.getDialogId();
            }
        }
        return null;
    }

    public static List<Integer> getOccupantsIdsFromDialog(QBDialog dialog) {
        QBUser user = App.getInstance().getUser();
        List<Integer> occupantsList = dialog.getOccupants();
        occupantsList.remove(user.getId());
        return occupantsList;
    }

    public static String getAttachUrlFromQBChatMessage(QBChatMessage chatMessage) {
        List<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(chatMessage.getAttachments());
        if (!attachmentsList.isEmpty()) {
            return attachmentsList.get(attachmentsList.size() - 1).getUrl();
        }
        return Consts.EMPTY_STRING;
    }

    public static String getAttachUrlFromQBChatMessage(QBHistoryMessage chatMessage) {
        List<QBAttachment> attachmentsList = new ArrayList<QBAttachment>(chatMessage.getAttachments());
        if (!attachmentsList.isEmpty()) {
            return attachmentsList.get(attachmentsList.size() - 1).getUrl();
        }
        return Consts.EMPTY_STRING;
    }

    public static QBDialog parseDialogFromMessage(QBChatMessage chatMessage) {
        String dialogId = chatMessage.getProperty(PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String dialogTypeCode = chatMessage.getProperty(PROPERTY_DIALOG_TYPE_CODE);

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setOccupantsIds(parseOccupantIdsFromString(occupantsIds));
        dialog.setName(dialogName);
        dialog.setType(QBDialogType.parseByCode(Integer.parseInt(dialogTypeCode)));
        dialog.setUnreadMessageCount(0);
        return dialog;
    }

    private static ArrayList<Integer> parseOccupantIdsFromString(String occupantIds) {
        ArrayList<Integer> occupantIdsList = new ArrayList<Integer>();
        String[] occupantIdsArray = occupantIds.split(OCCUPANT_IDS_DIVIDER);
        for (String occupantId : occupantIdsArray) {
            occupantIdsList.add(Integer.valueOf(occupantId));
        }
        return occupantIdsList;
    }

    public static QBChatMessage createRoomNotificationMessage(QBDialog dialog) {
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = occupantIdsToStringFromDialog(dialog);
        String dialogName = dialog.getName();
        String dialogTypeCode = String.valueOf(dialog.getType().ordinal());

        QBChatMessage message = new QBChatMessage();
        message.setProperty(PROPERTY_DIALOG_ID, dialogId);
        message.setProperty(PROPERTY_ROOM_JID, roomJid);
        message.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        message.setProperty(PROPERTY_ROOM_NAME, dialogName);
        message.setProperty(PROPERTY_DIALOG_TYPE_CODE, dialogTypeCode);
        return message;
    }

    private static String occupantIdsToStringFromDialog(QBDialog dialog) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, dialog.getOccupants());
    }

    public static boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_DIALOG_ID) != null;
    }
}