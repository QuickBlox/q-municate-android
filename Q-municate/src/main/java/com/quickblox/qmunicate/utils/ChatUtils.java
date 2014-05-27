package com.quickblox.qmunicate.utils;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;

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
}