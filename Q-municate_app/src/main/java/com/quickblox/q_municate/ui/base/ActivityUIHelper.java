package com.quickblox.q_municate.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.GroupDialogActivity;
import com.quickblox.q_municate.ui.chats.PrivateDialogActivity;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.service.QBServiceConsts;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ActivityUIHelper {

    private Activity activity;
    private View newMessageView;
    private TextView newMessageTextView;
    private TextView senderMessageTextView;
    private Button replyMessageButton;

    private User senderUser;
    private QBDialog messagesDialog;
    private boolean isPrivateMessage;

    public ActivityUIHelper(Activity activity) {
        this.activity = activity;
        initUI();
        initListeners();
    }

    private void initUI() {
        newMessageView = activity.getLayoutInflater().inflate(R.layout.list_item_new_message, null);
        newMessageTextView = (TextView) newMessageView.findViewById(R.id.message_textview);
        senderMessageTextView = (TextView) newMessageView.findViewById(R.id.sender_textview);
        replyMessageButton = (Button) newMessageView.findViewById(R.id.replay_button);
    }

    private void initListeners() {
        replyMessageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                replyMessage();
            }
        });
    }

    protected void onReceiveMessage(Bundle extras) {
        senderUser = (User) extras.getSerializable(QBServiceConsts.EXTRA_USER);
        String message = extras.getString(QBServiceConsts.EXTRA_CHAT_MESSAGE);
        String dialogId = extras.getString(QBServiceConsts.EXTRA_DIALOG_ID);
        messagesDialog = ChatDatabaseManager.getDialogByDialogId(activity, dialogId);
        isPrivateMessage = extras.getBoolean(QBServiceConsts.EXTRA_IS_PRIVATE_MESSAGE);
        if (messagesDialog != null) {
            showNewMessageAlert(senderUser, message);
        }
    }

    public void showNewMessageAlert(User senderUser, String message) {
        newMessageTextView.setText(message);
        senderMessageTextView.setText(senderUser.getFullName());
        Crouton.cancelAllCroutons();
        Crouton.show(activity, newMessageView);
    }

    protected void replyMessage() {
        if (isPrivateMessage) {
            startPrivateChatActivity();
        } else {
            startGroupChatActivity();
        }
        Crouton.cancelAllCroutons();
    }

    private void startPrivateChatActivity() {
        PrivateDialogActivity.start(activity, senderUser, messagesDialog);
    }

    private void startGroupChatActivity() {
        GroupDialogActivity.start(activity, messagesDialog);
    }
}