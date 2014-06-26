package com.quickblox.qmunicate.utils;

import android.content.Context;
import android.text.TextUtils;

import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBMessage;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.model.Friend;

import java.util.ArrayList;
import java.util.List;

import static com.quickblox.module.chat.model.QBDialogType.parseByCode;

public class ChatUtils {

    public static final String OCCUPANT_IDS_DIVIDER = ",";

    private static final String PROPERTY_OCCUPANTS_IDS = "occupants_ids";
    private static final String PROPERTY_ROOM_NAME = "name";
    private static final String PROPERTY_DIALOG_TYPE_CODE = "type";
    private static final String PROPERTY_ROOM_JID = "room_jid";
    private static final String PROPERTY_DIALOG_ID = "_id";

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

    public static QBDialog parseDialogFromMessage(QBMessage chatMessage, String lastMessage, long dateSent) {
        String dialogId = chatMessage.getProperty(PROPERTY_DIALOG_ID);
        String roomJid = chatMessage.getProperty(PROPERTY_ROOM_JID);
        String occupantsIds = chatMessage.getProperty(PROPERTY_OCCUPANTS_IDS);
        String dialogName = chatMessage.getProperty(PROPERTY_ROOM_NAME);
        String dialogTypeCode = chatMessage.getProperty(PROPERTY_DIALOG_TYPE_CODE);

        QBDialog dialog = new QBDialog(dialogId);
        dialog.setRoomJid(roomJid);
        dialog.setOccupantsIds(getOccupantsIdsListFromString(occupantsIds));
        dialog.setName(dialogName);
        dialog.setType(parseByCode(Integer.parseInt(dialogTypeCode)));
        dialog.setLastMessage(lastMessage);
        dialog.setLastMessageDateSent(dateSent);
        dialog.setUnreadMessageCount(Consts.ZERO_INT_VALUE);
        return dialog;
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
        String dialogId = String.valueOf(dialog.getDialogId());
        String roomJid = dialog.getRoomJid();
        String occupantsIds = getOccupantsIdsStringFromList(dialog.getOccupants());
        String dialogName = dialog.getName();
        String dialogTypeCode = String.valueOf(dialog.getType().ordinal());

        QBUser user = AppSession.getSession().getUser();
        QBChatMessage chatMessage = new QBChatMessage();
        chatMessage.setBody(context.getResources().getString(R.string.user_created_room, user.getFullName()));
        chatMessage.setProperty(PROPERTY_DIALOG_ID, dialogId);
        chatMessage.setProperty(PROPERTY_ROOM_JID, roomJid);
        chatMessage.setProperty(PROPERTY_OCCUPANTS_IDS, occupantsIds);
        chatMessage.setProperty(PROPERTY_ROOM_NAME, dialogName);
        chatMessage.setProperty(PROPERTY_DIALOG_TYPE_CODE, dialogTypeCode);
        return chatMessage;
    }

    public static String getOccupantsIdsStringFromList(List<Integer> occupantIdsList) {
        return TextUtils.join(OCCUPANT_IDS_DIVIDER, occupantIdsList);
    }

    public static boolean isNotificationMessage(QBChatMessage chatMessage) {
        return chatMessage.getProperty(PROPERTY_DIALOG_ID) != null;
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

    public static ArrayList<Integer> getFriendIdsList(List<Friend> friendList) {
        ArrayList<Integer> friendIdsList = new ArrayList<Integer>();
        for (Friend friend : friendList) {
            friendIdsList.add(friend.getId());
        }
        return friendIdsList;
    }

    public static String getAttachUrlIfExists(QBChatMessage chatMessage) {
        String attachURL = Consts.EMPTY_STRING;
        if (TextUtils.isEmpty(chatMessage.getBody())) {
            attachURL = getAttachUrlFromMessage(new ArrayList<QBAttachment>(
                    chatMessage.getAttachments()));
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
}