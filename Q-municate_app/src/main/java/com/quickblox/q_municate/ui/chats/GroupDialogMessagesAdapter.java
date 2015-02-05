package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.db.managers.UsersDatabaseManager;
import com.quickblox.q_municate_core.models.MessageCache;
import com.quickblox.q_municate_core.models.User;
import com.quickblox.q_municate_core.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate.ui.chats.emoji.EmojiTextView;
import com.quickblox.q_municate.ui.views.MaskedImageView;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate.utils.DateUtils;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(Context context, Cursor cursor,
            ChatUIHelperListener chatUIHelperListener, QBDialog dialog) {
        super(context, cursor);
        this.chatUIHelperListener = chatUIHelperListener;
        this.dialog = dialog;
    }

    private int getItemViewType(Cursor cursor) {
        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);
        boolean ownMessage = isOwnMessage(messageCache.getSenderId());
        boolean friendsRequestMessage = messageCache.getMessagesNotificationType() != null;

        if (!friendsRequestMessage) {
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
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return COMMON_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();

        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);
        boolean ownMessage = isOwnMessage(messageCache.getSenderId());

        if (messageCache.getMessagesNotificationType() == null) {

            if (ownMessage) {
                view = layoutInflater.inflate(R.layout.list_item_message_own, null, true);
            } else {
                view = layoutInflater.inflate(R.layout.list_item_group_message_opponent, null, true);
                viewHolder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
                setViewVisibility(viewHolder.avatarImageView, View.VISIBLE);
                viewHolder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
                setViewVisibility(viewHolder.nameTextView, View.VISIBLE);
            }

            viewHolder.attachMessageRelativeLayout = (RelativeLayout) view.findViewById(R.id.attach_message_relativelayout);
            viewHolder.timeAttachMessageTextView = (TextView) view.findViewById(R.id.time_attach_message_textview);
            viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(R.id.progress_relativelayout);
            viewHolder.textMessageView = view.findViewById(R.id.text_message_view);
            viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
            viewHolder.attachImageView = (MaskedImageView) view.findViewById(R.id.attach_imageview);
            viewHolder.timeTextMessageTextView = (TextView) view.findViewById(R.id.time_text_message_textview);
            viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
            viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.vertical_progressbar));
            viewHolder.centeredProgressBar = (ProgressBar) view.findViewById(R.id.centered_progressbar);
        } else {
            view = layoutInflater.inflate(R.layout.list_item_notification_message, null, true);

            viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
            viewHolder.timeTextMessageTextView = (TextView) view.findViewById(
                    R.id.time_text_message_textview);
        }

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String avatarUrl = null;
        String senderName;

        MessageCache messageCache = ChatDatabaseManager.getMessageCacheFromCursor(cursor);

        boolean ownMessage = isOwnMessage(messageCache.getSenderId());
        boolean notificationMessage = messageCache.getMessagesNotificationType() != null;

        if (notificationMessage) {
            viewHolder.messageTextView.setText(messageCache.getMessage());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
        } else {
            resetUI(viewHolder);

            if (ownMessage) {
                avatarUrl = getAvatarUrlForCurrentUser();
            } else {
                User senderFriend = UsersDatabaseManager.getUserById(context, messageCache.getSenderId());
                if (senderFriend != null) {
                    senderName = senderFriend.getFullName();
                    avatarUrl = getAvatarUrlForFriend(senderFriend);
                } else {
                    senderName = messageCache.getSenderId() + ConstsCore.EMPTY_STRING;
                }
                viewHolder.nameTextView.setTextColor(getTextColor(messageCache.getSenderId()));
                viewHolder.nameTextView.setText(senderName);
            }

            if (!TextUtils.isEmpty(messageCache.getAttachUrl())) {
                viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
                setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
                displayAttachImage(messageCache.getAttachUrl(), viewHolder);
            } else {
                setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
                viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
                viewHolder.messageTextView.setText(messageCache.getMessage());
            }
        }

        if (!messageCache.isRead() && !ownMessage) {
            messageCache.setRead(true);
            QBUpdateStatusMessageCommand.start(context, dialog, messageCache, false);
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
    }
}