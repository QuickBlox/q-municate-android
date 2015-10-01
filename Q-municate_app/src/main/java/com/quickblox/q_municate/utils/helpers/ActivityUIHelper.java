package com.quickblox.q_municate.utils.helpers;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.chats.BaseDialogActivity;
import com.quickblox.q_municate.ui.activities.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.activities.chats.PrivateDialogActivity;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

public class ActivityUIHelper {

    private BaseActivity baseActivity;

    private User senderUser;
    private Dialog messagesDialog;
    private boolean isPrivateMessage;

    public ActivityUIHelper(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public void showChatMessageNotification(Bundle extras) {
        senderUser = (User) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        if (isMessagesDialogCorrect(dialogId)) {
            message = baseActivity.getString(R.string.glgm_snackbar_new_message_title, senderUser.getFullName(), message);
            showNewNotification(senderUser, message);
        }
    }

    private boolean isMessagesDialogCorrect(String dialogId) {
        messagesDialog = DataManager.getInstance().getDialogDataManager().getByDialogId(dialogId);
        return messagesDialog != null;
    }

    public void showContactRequestNotification(Bundle extras) {
        int senderUserId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
        senderUser = DataManager.getInstance().getUserDataManager().get(senderUserId);
        String message = extras.getString(QBServiceConsts.EXTRA_MESSAGE);
        DialogOccupant dialogOccupant = DataManager.getInstance().getDialogOccupantDataManager().getDialogOccupantForPrivateChat(senderUserId);

        if (dialogOccupant != null) {
            String dialogId = dialogOccupant.getDialog().getDialogId();
            isPrivateMessage = true;
            if (isMessagesDialogCorrect(dialogId)) {
                message = baseActivity.getString(R.string.glgm_snackbar_new_contact_request_title, senderUser.getFullName());
                showNewNotification(senderUser, message);
            }
        }
    }

    public void showNewNotification(User senderUser, String message) {
        if (senderUser == null) {
            return;
        }

        baseActivity.hideSnackBar();
        baseActivity.showSnackbar(
                message,
                Snackbar.LENGTH_LONG,
                R.string.dlg_reply,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDialog();
                    }
                });
    }

    protected void showDialog() {
        if (baseActivity instanceof BaseDialogActivity) {
            baseActivity.finish();
        }

        if (isPrivateMessage) {
            startPrivateChatActivity();
        } else {
            startGroupChatActivity();
        }
    }

    private void startPrivateChatActivity() {
        PrivateDialogActivity.start(baseActivity, senderUser, messagesDialog);
    }

    private void startGroupChatActivity() {
        GroupDialogActivity.start(baseActivity, messagesDialog);
    }
}