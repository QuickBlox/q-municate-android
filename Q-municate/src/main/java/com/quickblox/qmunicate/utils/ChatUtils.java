package com.quickblox.qmunicate.utils;

import com.quickblox.module.chat.QBChatMessage;
import com.quickblox.module.chat.QBHistoryMessage;
import com.quickblox.module.chat.model.QBAttachment;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;

import java.util.ArrayList;
import java.util.List;

public class ChatUtils {

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
}