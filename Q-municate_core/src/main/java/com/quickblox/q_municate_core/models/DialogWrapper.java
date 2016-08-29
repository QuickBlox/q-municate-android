package com.quickblox.q_municate_core.models;

import android.content.Context;
import android.view.View;

import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.users.model.QBUser;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Andrey on 8/26/16.
 */
public class DialogWrapper implements Serializable {

    private Dialog dialog;
    private User opponentUser;
    private long totalCount;
    private String lastMessage;

    public DialogWrapper(Context context, DataManager dataManager, Dialog dialog) {
        this.dialog = dialog;
        transform(context, dataManager);
    }

    private void transform(Context context, DataManager dataManager){
        QBUser currentUser = AppSession.getSession().getUser();

        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(dialog.getDialogId());

        if (Dialog.Type.PRIVATE.equals(dialog.getType())) {
            opponentUser = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(currentUser), dialogOccupantsList);
        }else{
            dataManager.getDialogDataManager().deleteById(dialog.getDialogId());
        }

        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);
        long unreadMessages = dataManager.getMessageDataManager().getCountUnreadMessages(dialogOccupantsIdsList, currentUser.getId());
        long unreadDialogNotifications = dataManager.getDialogNotificationDataManager().getCountUnreadDialogNotifications(dialogOccupantsIdsList, currentUser.getId());

        totalCount = unreadMessages + unreadDialogNotifications;

        Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager().getLastDialogNotificationByDialogId(dialogOccupantsIdsList);

        lastMessage = ChatUtils.getDialogLastMessage(context.getResources().getString(R.string.cht_notification_message), message, dialogNotification);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public User getOpponentUser() {
        return opponentUser;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
