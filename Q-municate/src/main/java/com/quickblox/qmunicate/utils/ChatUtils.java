package com.quickblox.qmunicate.utils;

import android.text.TextUtils;

import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.model.ChatCache;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.module.chat.model.QBDialogType.parseByCode;

public class ChatUtils {

    public static final String OCCUPANT_IDS_DIVIDER = ",";

    private static final String LEFT_BRACKET = "[";
    private static final String RIGHT_BRACKET = "]";
    private static final String SPACE = " ";
    private static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    private static final String PROPERTY_ROOM_NAME = "name";
    private static final String PROPERTY_DIALOG_TYPE_CODE = "type";
    private static final String PROPERTY_ROOM_JID = "room_jid";
    private static final String PROPERTY_DIALOG_ID = "_id";

    public static int getOccupantIdFromArray(String[] occupantsIdsArray) {
        QBUser user = App.getInstance().getUser();
        int resultId = Consts.ZERO_VALUE;
        for (String idString : occupantsIdsArray) {
            int intId = Integer.parseInt(idString);
            if (intId != user.getId()) {
                resultId = intId;
            }
        }
        return resultId;
    }

    public static QBDialog getQBDialogFromChatCache(ChatCache chatCache) {
        QBDialog dialog = new QBDialog(chatCache.getDialogId());
        dialog.setRoomJid(chatCache.getRoomJidId());
        dialog.setName(chatCache.getName());
        dialog.setUnreadMessageCount(chatCache.getCountUnreadMessages());
        dialog.setLastMessage(chatCache.getLastMessage());
        return dialog;
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
        dialog.setType(parseByCode(Integer.parseInt(dialogTypeCode)));
        dialog.setUnreadMessageCount(Consts.ZERO_VALUE);
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
        String occupantsIds = occupantIdsToStringFromArray(getOccupantsStringArray(dialog.getOccupants()));
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

    public static String occupantIdsToStringFromArray(String[] occupantsArray) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, occupantsArray);
    }

    public static boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_DIALOG_ID) != null;
    }

    public static String deleteBracketsFromOccupantsArray(String occupantsIds) {
        return occupantsIds.replace(LEFT_BRACKET, Consts.EMPTY_STRING).replace(RIGHT_BRACKET,
                Consts.EMPTY_STRING).replace(SPACE, Consts.EMPTY_STRING);
    }

    public static boolean isGroupMessageByChatId(Object chatId) {
        if (chatId instanceof String) {
            return true;
        } else if (chatId instanceof Integer) {
            return false;
        }
        return false;
    }

    public static List<ChatCache> getChatCacheList(List<QBDialog> dialogsList) {
        List<ChatCache> chatCacheList = new ArrayList<ChatCache>(dialogsList.size());
        for (QBDialog dialog : dialogsList) {
            chatCacheList.add(new ChatCache(dialog.getDialogId(), dialog.getRoomJid(), dialog.getName(),
                    dialog.getUnreadMessageCount(), dialog.getLastMessage(), getOccupantsStringArray(
                    dialog.getOccupants()), dialog.getType().ordinal()
            ));
        }
        return chatCacheList;
    }

    public static ChatCache getChatCacheFromQBDialog(QBDialog dialog) {
        return new ChatCache(dialog.getDialogId(), dialog.getRoomJid(), dialog.getName(),
                dialog.getUnreadMessageCount(), dialog.getLastMessage(), getOccupantsStringArray(
                dialog.getOccupants()), dialog.getType().ordinal()
        );
    }

    private static String[] getOccupantsStringArray(ArrayList<Integer> occupantsList) {
        String[] occupantsArray = new String[occupantsList.size()];
        for (int i = 0; i < occupantsList.size(); i++) {
            occupantsArray[i] = String.valueOf(occupantsList.get(i));
        }
        return occupantsArray;
    }
}