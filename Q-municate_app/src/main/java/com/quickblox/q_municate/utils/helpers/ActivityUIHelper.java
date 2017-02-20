package com.quickblox.q_municate.utils.helpers;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.view.View;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.chats.BaseDialogActivity;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;

public class ActivityUIHelper {

    private BaseActivity baseActivity;
    private QMUser senderUser;
    private QBChatDialog messagesDialog;
    private String message;
    private boolean isPrivateMessage;

    public ActivityUIHelper(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public void showChatMessageNotification(Bundle extras) {
        senderUser = (QMUser) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        if (isMessagesDialogCorrect(dialogId) && senderUser != null) {
            message = baseActivity.getString(R.string.snackbar_new_message_title, senderUser.getFullName(), message);
            if (!TextUtils.isEmpty(message)) {
                showNewNotification();
            }
        }
    }

    private boolean isMessagesDialogCorrect(String dialogId) {
        Dialog dialog = DataManager.getInstance().getDialogDataManager().getByDialogId(dialogId);
        if (messagesDialog!= null){
            messagesDialog = ChatUtils.createQBDialogFromLocalDialog(DataManager.getInstance(), dialog);
        }
        return messagesDialog != null;
    }

    public void showContactRequestNotification(Bundle extras) {
        int senderUserId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
        senderUser = QMUserService.getInstance().getUserCache().get((long)senderUserId);
        message = extras.getString(QBServiceConsts.EXTRA_MESSAGE);
        DialogOccupant dialogOccupant = DataManager.getInstance().getDialogOccupantDataManager().getDialogOccupantForPrivateChat(senderUserId);

        if (dialogOccupant != null && senderUser != null) {
            String dialogId = dialogOccupant.getDialog().getDialogId();
            isPrivateMessage = true;
            if (isMessagesDialogCorrect(dialogId)) {
                message = baseActivity.getString(R.string.snackbar_new_contact_request_title, senderUser.getFullName());
                if (!TextUtils.isEmpty(message)) {
                    showNewNotification();
                }
            }
        }
    }

    public void showNewNotification() {
        baseActivity.hideSnackBar();
        baseActivity.showSnackbar(message, Snackbar.LENGTH_LONG, R.string.dialog_reply,
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDialog();
                    }
                });
    }

    private void showDialog() {
        if (baseActivity instanceof BaseDialogActivity) {
            baseActivity.finish();
        }

        if (isPrivateMessage) {
            baseActivity.startPrivateChatActivity(senderUser, messagesDialog);
        } else {
            baseActivity.startGroupChatActivity(messagesDialog);
        }
    }
}