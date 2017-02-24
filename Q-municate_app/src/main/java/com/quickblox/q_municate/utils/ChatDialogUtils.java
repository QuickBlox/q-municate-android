package com.quickblox.q_municate.utils;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;

public class ChatDialogUtils {

    public static String getTitleForChatDialog(QBChatDialog chatDialog, DataManager dataManager) {
        if (QBDialogType.GROUP.equals(chatDialog.getType())) {
            return chatDialog.getName();
        } else {
            Integer currentUserId = AppSession.getSession().getUser().getId();
            return ChatUtils.getFullNameById(dataManager, getPrivateChatOpponentId(chatDialog, currentUserId));
        }
    }

    public static Integer getPrivateChatOpponentId(QBChatDialog chatDialog, Integer currentUserId){
        for (Integer opponentID : chatDialog.getOccupants()){
            if (!opponentID.equals(currentUserId)){
                return opponentID;
            }
        }

        return 0;
    }
}
