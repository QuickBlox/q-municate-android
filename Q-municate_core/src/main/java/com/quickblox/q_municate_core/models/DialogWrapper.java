package com.quickblox.q_municate_core.models;

import android.content.Context;
import android.util.Log;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate_core.R;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.UserFriendUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.io.Serializable;
import java.util.List;

public class DialogWrapper implements Serializable {

    private static final String TAG = DialogWrapper.class.getSimpleName();
    private QBChatDialog chatDialog;
    private QMUser opponentUser;
    private long totalCount;
    private String lastMessage;

    public DialogWrapper(Context context, DataManager dataManager, QBChatDialog chatDialog) {
        this.chatDialog = chatDialog;
        transform(context, dataManager);
    }

    private void transform(Context context, DataManager dataManager){
        QBUser currentUser = AppSession.getSession().getUser();
        List<DialogOccupant> dialogOccupantsList = dataManager.getDialogOccupantDataManager().getDialogOccupantsListByDialogId(chatDialog.getDialogId());
        List<Long> dialogOccupantsIdsList = ChatUtils.getIdsFromDialogOccupantsList(dialogOccupantsList);

        fillOpponentUser(context, dataManager, dialogOccupantsList, currentUser);
        fillTotalCount(context, dataManager, dialogOccupantsIdsList, currentUser);
        fillLastMessage(context, dataManager, dialogOccupantsIdsList);
    }

    private void fillOpponentUser(Context context, DataManager dataManager,  List<DialogOccupant> dialogOccupantsList,  QBUser currentUser ){
        if (QBDialogType.PRIVATE.equals(chatDialog.getType())) {
            opponentUser = ChatUtils.getOpponentFromPrivateDialog(UserFriendUtils.createLocalUser(currentUser), dialogOccupantsList);

            if (opponentUser.getFullName() == null) {
                dataManager.getQBChatDialogDataManager().deleteById(chatDialog.getDialogId());
            }
        }
    }

    private void fillTotalCount(Context context, DataManager dataManager,  List<Long> dialogOccupantsIdsList,  QBUser currentUser){
        long unreadMessages = dataManager.getMessageDataManager().getCountUnreadMessages(dialogOccupantsIdsList, currentUser.getId());
        long unreadDialogNotifications = dataManager.getDialogNotificationDataManager().getCountUnreadDialogNotifications(dialogOccupantsIdsList, currentUser.getId());
        if (unreadMessages > 0) {
            Log.i(TAG, "chat Dlg:" + chatDialog.getName() + ", unreadMessages = " + unreadMessages);
        }

        if (unreadDialogNotifications > 0) {
            Log.i(TAG, "unreadDialogNotifications = " + unreadDialogNotifications);
        }

        totalCount = unreadMessages + unreadDialogNotifications;
    }

    private void fillLastMessage(Context context, DataManager dataManager, List<Long> dialogOccupantsIdsList){
        Message message = dataManager.getMessageDataManager().getLastMessageWithTempByDialogId(dialogOccupantsIdsList);
        DialogNotification dialogNotification = dataManager.getDialogNotificationDataManager().getLastDialogNotificationByDialogId(dialogOccupantsIdsList);

        lastMessage = ChatUtils.getDialogLastMessage(context.getResources().getString(R.string.cht_notification_message), message, dialogNotification);
    }

    public QBChatDialog getChatDialog() {
        return chatDialog;
    }

    public QMUser getOpponentUser() {
        return opponentUser;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }
}
