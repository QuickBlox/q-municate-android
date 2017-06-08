package com.quickblox.q_municate_core.models;

import android.content.Context;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.io.Serializable;
import java.util.List;

/**
 * Created by pelipets on 3/13/17.
 */

public class DialogSearchWrapper implements Serializable {

    private QBChatDialog chatDialog;

    private QMUser opponentUser;

    private String label;

    public DialogSearchWrapper (Context context, DataManager dataManager, QBChatDialog chatDialog) {
        this.chatDialog = chatDialog;
        transform(context, dataManager);
    }

    private void transform(Context context, DataManager dataManager){
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(chatDialog.getDialogId());

        if (QBDialogType.PRIVATE.equals(chatDialog.getType())) {
            QMUser currentUser = UserFriendUtils.createLocalUser(AppSession.getSession().getUser());
            opponentUser = ChatUtils.getOpponentFromPrivateDialog(currentUser, dialogOccupantsList);
        } else {
            List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
            Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(dialogOccupantsIdsList);
            DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager().getLastDialogNotificationByDialogId(dialogOccupantsIdsList);
            label = ChatUtils.getDialogLastMessage(
                    context.getResources().getString(R.string.cht_notification_message),
                    message,
                    dialogNotification);
        }
    }

    public QBChatDialog getChatDialog() {
        return chatDialog;
    }

    public QMUser getOpponentUser() {
        return opponentUser;
    }

    public String getLabel() {
        return label;
    }
}
