package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.chats.emoji.EmojiTextView;
import com.quickblox.q_municate.ui.views.MaskedImageView;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DatabaseManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.Message;
import com.quickblox.q_municate_db.models.User;

import java.util.List;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(Context context, List<Message> objectsList,
            ChatUIHelperListener chatUIHelperListener, Dialog dialog) {
        super(context, objectsList);
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    private int getItemViewType(Message message) {

        boolean ownMessage = message.isIncoming(currentQBUser.getId());
        //        boolean friendsRequestMessage = messageCache.getMessagesNotificationType() != null;

        //        if (!friendsRequestMessage) {
        if (ownMessage) {
            return TYPE_OWN_MESSAGE;
        } else {
            return TYPE_OPPONENT_MESSAGE;
        }
        //        } else {
        //            return TYPE_REQUEST_MESSAGE;
        //        }
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

        Message message = getItem(position);
        boolean ownMessage = !message.isIncoming(currentQBUser.getId());

        if (view == null) {
            viewHolder = new ViewHolder();

            // TODO temp
            //            if (message.getMessagesNotificationType() == null) {
            if (ownMessage) {
                view = layoutInflater.inflate(R.layout.list_item_message_own, null, true);
            } else {
                view = layoutInflater.inflate(R.layout.list_item_group_message_opponent, null, true);
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

            //            } else {
            //            view = layoutInflater.inflate(R.layout.list_item_notification_message, null, true);
            //
            //            viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
            //            viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
            //                    R.id.time_text_message_textview);
            //            }

            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String avatarUrl = null;
        String senderName = "name";

        resetUI(viewHolder);

        if (ownMessage) {
            //            avatarUrl = currentQBUser.;
        } else {
            User senderFriend = DatabaseManager.getInstance().getUserManager().get(
                    message.getDialogOccupant().getUser().getUserId());
            if (senderFriend != null) {
                senderName = senderFriend.getFullName();
                //                avatarUrl = getAvatarUrlForFriend(senderFriend);
            } else {
                //                senderName = messageCache.getSenderId() + ConstsCore.EMPTY_STRING;
            }
            viewHolder.nameTextView.setTextColor(getTextColor(
                    message.getDialogOccupant().getUser().getUserId()));
            viewHolder.nameTextView.setText(senderName);
        }

        if (message.getAttachment() != null) {
            viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(
                    message.getCreatedDate()));
            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            displayAttachImage(message.getAttachment().getRemoteUrl(), viewHolder);
        } else {
            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(message.getCreatedDate()));
            viewHolder.messageTextView.setText(message.getBody());
        }

        if (!Message.State.READ.equals(message.getState()) && !ownMessage) {
            message.setState(Message.State.READ);
            QBUpdateStatusMessageCommand.start(context, ChatUtils.createQBDialogFromLocalDialog(dialog),
                    message, true);
        }

        //        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);

        return view;
    }
}