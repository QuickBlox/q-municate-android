package com.quickblox.q_municate.utils.helpers;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.chats.BaseDialogActivity;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogOccupant;
import com.quickblox.q_municate_db.models.User;

import java.util.Timer;
import java.util.TimerTask;

public class ActivityUIHelper {

    private static final int SHOWING_NOTIFICATION_DELAY = 500;

    private BaseActivity baseActivity;
    private User senderUser;
    private Dialog messagesDialog;
    private String message;
    private boolean isPrivateMessage;
    private Timer notificationTimer;

    public ActivityUIHelper(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    public void showChatMessageNotification(Bundle extras) {
        senderUser = (User) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        if (isMessagesDialogCorrect(dialogId) && senderUser != null) {
            message = baseActivity.getString(R.string.glgm_snackbar_new_message_title, senderUser.getFullName(), message);
            checkShowingNotification();
        }
    }

    private boolean isMessagesDialogCorrect(String dialogId) {
        messagesDialog = DataManager.getInstance().getDialogDataManager().getByDialogId(dialogId);
        return messagesDialog != null;
    }

    public void showContactRequestNotification(Bundle extras) {
        int senderUserId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
        senderUser = DataManager.getInstance().getUserDataManager().get(senderUserId);
        message = extras.getString(QBServiceConsts.EXTRA_MESSAGE);
        DialogOccupant dialogOccupant = DataManager.getInstance().getDialogOccupantDataManager().getDialogOccupantForPrivateChat(senderUserId);

        if (dialogOccupant != null && senderUser != null) {
            String dialogId = dialogOccupant.getDialog().getDialogId();
            isPrivateMessage = true;
            if (isMessagesDialogCorrect(dialogId)) {
                message = baseActivity.getString(R.string.glgm_snackbar_new_contact_request_title, senderUser.getFullName());
                checkShowingNotification();
            }
        }
    }

    public void showNewNotification() {
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

    private void checkShowingNotification() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
        notificationTimer = new Timer();
        notificationTimer.schedule(new ShowingNotificationTimerTask(), SHOWING_NOTIFICATION_DELAY);
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

    private class ShowingNotificationTimerTask extends TimerTask {

        @Override
        public void run() {
            baseActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showNewNotification();
                }
            });
        }
    }
}