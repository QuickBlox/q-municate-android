package com.quickblox.q_municate.ui.adapters.chats;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.ChatUIHelperListener;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.State;

import java.util.List;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(Activity activity, List<CombinationMessage> objectsList,
            ChatUIHelperListener chatUIHelperListener, Dialog dialog) {
        super(activity, objectsList);
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    @Override
    public PrivateDialogMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case TYPE_REQUEST_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_notification_message, viewGroup, false));
            case TYPE_OWN_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_message_own, viewGroup, false));
            case TYPE_OPPONENT_MESSAGE:
                return new ViewHolder(this, layoutInflater.inflate(R.layout.item_group_message_opponent, viewGroup, false));
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

    @Override
    public void onBindViewHolder(BaseClickListenerViewHolder<CombinationMessage> baseClickListenerViewHolder, int position) {
        ViewHolder viewHolder = (ViewHolder) baseClickListenerViewHolder;

        CombinationMessage combinationMessage = getItem(position);
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());
        boolean notificationMessage = combinationMessage.getNotificationType() != null;

        String avatarUrl = null;
        String senderName;

        if (notificationMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
        } else {

            resetUI(viewHolder);

            if (ownMessage) {
                avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
            } else {
                senderName = combinationMessage.getDialogOccupant().getUser().getFullName();
                avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
                viewHolder.nameTextView.setTextColor(colorUtils.getRandomTextColorById(combinationMessage.getDialogOccupant().getUser().getUserId()));
                viewHolder.nameTextView.setText(senderName);
            }

            if (combinationMessage.getAttachment() != null) {
                viewHolder.timeAttachMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
                setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
                displayAttachImage(combinationMessage.getAttachment().getRemoteUrl(), viewHolder);
            } else {
                setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
                viewHolder.timeTextMessageTextView.setText(
                        DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
                viewHolder.messageTextView.setText(combinationMessage.getBody());
            }
        }

        if (!State.READ.equals(combinationMessage.getState()) && !ownMessage) {
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(context,
                    ChatUtils.createQBDialogFromLocalDialog(dialog), combinationMessage, false);
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
    }
}