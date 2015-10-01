package com.quickblox.q_municate.ui.adapters.chats;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.core.listeners.ChatUIHelperListener;
import com.quickblox.q_municate.ui.views.emoji.EmojiTextView;
import com.quickblox.q_municate.ui.views.maskedimageview.MaskedImageView;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.State;

import java.util.List;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(Context context, List<CombinationMessage> objectsList,
            ChatUIHelperListener chatUIHelperListener, Dialog dialog) {
        super(context, objectsList);
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    private int getItemViewType(CombinationMessage combinationMessage) {
        boolean ownMessage = combinationMessage.isIncoming(currentUser.getId());
        if (combinationMessage.getNotificationType() == null) {
            if (ownMessage) {
                return TYPE_OWN_MESSAGE;
            } else {
                return TYPE_OPPONENT_MESSAGE;
            }
        } else {
            return TYPE_REQUEST_MESSAGE;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return getItemViewType(getItem(position));
    }

    @Override
    public int getViewTypeCount() {
        return COMMON_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder viewHolder;

        CombinationMessage combinationMessage = getItem(position);
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());
        boolean notificationMessage = combinationMessage.getNotificationType() != null;

        if (view == null) {
            viewHolder = new ViewHolder();

            if (!notificationMessage) {
                if (ownMessage) {
                    view = layoutInflater.inflate(R.layout.item_message_own, null, true);
                } else {
                    view = layoutInflater.inflate(R.layout.item_group_message_opponent, null, true);
                    viewHolder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
                    setViewVisibility(viewHolder.avatarImageView, View.VISIBLE);
                    viewHolder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
                    setViewVisibility(viewHolder.nameTextView, View.VISIBLE);
                }

                viewHolder.attachMessageRelativeLayout = (RelativeLayout) view.findViewById(
                        R.id.attach_message_relativelayout);
                viewHolder.timeAttachMessageTextView = (TextView) view.findViewById(
                        R.id.time_attach_message_textview);
                viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(
                        R.id.progress_relativelayout);
                viewHolder.textMessageView = view.findViewById(R.id.text_message_view);
                viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
                viewHolder.attachImageView = (MaskedImageView) view.findViewById(R.id.attach_imageview);
                viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                        R.id.time_text_message_textview);
                viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
                viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(
                        R.drawable.vertical_progressbar));
                viewHolder.centeredProgressBar = (ProgressBar) view.findViewById(R.id.centered_progressbar);
            } else {
                view = layoutInflater.inflate(R.layout.item_notification_message, null, true);

                viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
                viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                        R.id.time_text_message_textview);
            }

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String avatarUrl = null;
        String senderName;

        if (notificationMessage) {
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(
                    combinationMessage.getCreatedDate()));
        } else {

            resetUI(viewHolder);

            if (ownMessage) {
                avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
            } else {
                senderName = combinationMessage.getDialogOccupant().getUser().getFullName();
                avatarUrl = combinationMessage.getDialogOccupant().getUser().getAvatar();
                viewHolder.nameTextView.setTextColor(
                        colorUtils.getRandomTextColorById(combinationMessage.getDialogOccupant().getUser().getUserId()));
                viewHolder.nameTextView.setText(senderName);
            }

            if (combinationMessage.getAttachment() != null) {
                viewHolder.timeAttachMessageTextView.setText(DateUtils.formatDateSimpleTime(
                        combinationMessage.getCreatedDate()));
                setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
                displayAttachImage(combinationMessage.getAttachment().getRemoteUrl(), viewHolder);
            } else {
                setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
                viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
                viewHolder.messageTextView.setText(combinationMessage.getBody());
            }
        }

        if (!State.READ.equals(combinationMessage.getState()) && !ownMessage) {
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(context,
                    ChatUtils.createQBDialogFromLocalDialog(dialog), combinationMessage, false);
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);

        return view;
    }
}