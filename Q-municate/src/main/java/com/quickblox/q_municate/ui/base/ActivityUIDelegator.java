package com.quickblox.q_municate.ui.base;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.model.User;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class ActivityUIDelegator extends ActivityDelegator {

    private View newMessageView;
    private TextView newMessageTextView;
    private TextView senderMessageTextView;
    private Button replyMessageButton;

    public ActivityUIDelegator(Context context) {
        super(context);
        initUI();
        initListeners();
    }

    private void initUI() {
        newMessageView = activity.getLayoutInflater().inflate(R.layout.list_item_new_message,
                null);
        newMessageTextView = (TextView) newMessageView.findViewById(R.id.message_textview);
        senderMessageTextView = (TextView) newMessageView.findViewById(R.id.sender_textview);
        replyMessageButton = (Button) newMessageView.findViewById(R.id.replay_button);
    }

    private void initListeners() {
        replyMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPrivateMessage) {
                    startPrivateChatActivity();
                } else {
                    startGroupChatActivity();
                }
                Crouton.cancelAllCroutons();
            }
        });
    }

    public void showNewMessageAlert(User senderUser, String message) {
        newMessageTextView.setText(message);
        senderMessageTextView.setText(senderUser.getFullName());
        Crouton.cancelAllCroutons();
        Crouton.show(activity, newMessageView);
    }
}