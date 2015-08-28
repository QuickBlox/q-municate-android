package com.quickblox.q_municate.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.chats.PrivateDialogActivity;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.PrefsHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ActivityUIHelper implements View.OnClickListener {

    private static String TAG = ActivityUIHelper.class.getSimpleName();

    private Activity activity;
    private User senderUser;
    private QBDialog messagesDialog;
    private boolean isPrivateMessage;
    private Crouton currentCrouton;
    private String dialogId;

    public ActivityUIHelper(Activity activity) {
        this.activity = activity;
    }

    protected void showChatMessageNotification(Bundle extras) {
        senderUser = (User) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        if (isMessagesDialogCorrect(dialogId) && senderUser != null) {
            showNewMessageAlert(senderUser, message);
        }
    }

    private boolean isMessagesDialogCorrect(String dialogId) {
        messagesDialog = ChatDatabaseManager.getDialogByDialogId(activity, dialogId);
        return messagesDialog != null;
    }

    protected void showContactRequestNotification(Bundle extras) {
        int senderUserId = extras.getInt(QBServiceConsts.EXTRA_USER_ID);
        senderUser = UsersDatabaseManager.getUserById(activity, senderUserId);
        String message = extras.getString(QBServiceConsts.EXTRA_MESSAGE);
        String dialogId = ChatDatabaseManager.getPrivateDialogIdByOpponentId(activity, senderUserId);
        isPrivateMessage = true;
        if (isMessagesDialogCorrect(dialogId) && senderUser != null) {
            showNewMessageAlert(senderUser, message);
        }
    }

    public void showNewMessageAlert(User senderUser, String message) {
        if (!PrefsHelper.getPrefsHelper().getPref(PrefsHelper.PREF_CROUTONS_DISABLED, false)) {

            // Init new crouton view instead of overusing it and add it in croutons queue
            // If we want overuse it we should handle crouton state to prevent changing data in it
            // while it is showing to user.
            View croutonView = initCroutonView(senderUser.getFullName(), message);

            // Create new crouton
                currentCrouton = Crouton.make(activity, croutonView);
                currentCrouton.show();
        }
    }

    private View initCroutonView(String fullName, String message) {
        View newMessageView = activity.getLayoutInflater().inflate(R.layout.list_item_new_message, null);
        TextView newMessageTextView = (TextView) newMessageView.findViewById(R.id.message_textview);
        TextView senderMessageTextView = (TextView) newMessageView.findViewById(R.id.sender_textview);
        Button notificationActionButton = (Button) newMessageView.findViewById(R.id.notification_action_button);
        newMessageView.setOnClickListener(this);
        notificationActionButton.setOnClickListener(this);

        newMessageTextView.setText(message);
        senderMessageTextView.setText(fullName);

        return newMessageView;
    }

    protected void showDialog() {
        if (isPrivateMessage) {
            startPrivateChatActivity();
        } else {
            startGroupChatActivity();
        }
    }

    private void startPrivateChatActivity() {
        PrivateDialogActivity.start(activity, senderUser, messagesDialog);
    }

    private void startGroupChatActivity() {
        GroupDialogActivity.start(activity, messagesDialog);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.crouton_notification_layout:
            case R.id.notification_action_button:
                Crouton.clearCroutonsForActivity(activity);
                showDialog();
                break;
            default:
                Log.d(TAG, "OnClickListener wasn't applied for view" + v);
                break;
        }
    }
}