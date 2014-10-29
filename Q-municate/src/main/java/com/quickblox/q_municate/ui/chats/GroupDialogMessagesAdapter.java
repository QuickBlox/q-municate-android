package com.quickblox.q_municate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.db.DatabaseManager;
import com.quickblox.q_municate.db.tables.MessageTable;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.qb.commands.QBUpdateStatusMessageReadCommand;
import com.quickblox.q_municate.ui.chats.emoji.EmojiTextView;
import com.quickblox.q_municate.ui.views.MaskedImageView;
import com.quickblox.q_municate.ui.views.RoundedImageView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;

public class GroupDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public GroupDialogMessagesAdapter(Context context, Cursor cursor,
            ScrollMessagesListener scrollMessagesListener, QBDialog dialog) {
        super(context, cursor);
        this.scrollMessagesListener = scrollMessagesListener;
        this.dialog = dialog;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();

        int senderId = cursor.getInt(cursor.getColumnIndex(MessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
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
        viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(R.id.progress_relativelayout);
        viewHolder.textMessageView = view.findViewById(R.id.text_message_view);
        viewHolder.messageTextView = (EmojiTextView) view.findViewById(R.id.message_textview);
        viewHolder.attachImageView = (MaskedImageView) view.findViewById(R.id.attach_imageview);
        viewHolder.timeTextMessageTextView = (TextView) view.findViewById(R.id.time_text_message_textview);
        viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
        viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(
                R.drawable.vertical_progressbar));
        viewHolder.centeredProgressBar = (ProgressBar) view.findViewById(R.id.centered_progressbar);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String avatarUrl = null;
        String senderName;

        MessageCache messageCache = DatabaseManager.getMessageCacheFromCursor(cursor);

        boolean ownMessage = isOwnMessage(messageCache.getSenderId());

        resetUI(viewHolder);

        viewHolder.messageDeliveryStatusImageView = (ImageView) view.findViewById(
                R.id.text_message_delivery_status_imageview);
        viewHolder.messageDeliveryStatusImageView = (ImageView) view.findViewById(
                R.id.attach_message_delivery_status_imageview);

        if (ownMessage) {
            avatarUrl = getAvatarUrlForCurrentUser();
        } else {
            setMessageStatus(view, viewHolder, R.id.text_message_delivery_status_imageview,
                    messageCache.isDelivered(), messageCache.isRead());
            User senderFriend = DatabaseManager.getUserById(context, messageCache.getSenderId());
            if (senderFriend != null) {
                senderName = senderFriend.getFullName();
                avatarUrl = getAvatarUrlForFriend(senderFriend);
            } else {
                senderName = messageCache.getSenderId() + Consts.EMPTY_STRING;
            }
            viewHolder.nameTextView.setTextColor(getTextColor(messageCache.getSenderId()));
            viewHolder.nameTextView.setText(senderName);
        }

        if (!TextUtils.isEmpty(messageCache.getAttachUrl())) {
//            setMessageStatus(view, viewHolder, R.id.attach_message_delivery_status_imageview, ownMessage,
//                    messageCache.isDelivered(), messageCache.isRead());
            viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            displayAttachImage(messageCache.getAttachUrl(), viewHolder);
        } else {
//            setMessageStatus(view, viewHolder, R.id.text_message_delivery_status_imageview, ownMessage,
//                    messageCache.isDelivered(), messageCache.isRead());
            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
            viewHolder.messageTextView.setText(messageCache.getMessage());
        }

        if (!messageCache.isRead() && ownMessage) {
            messageCache.setRead(true);
            QBUpdateStatusMessageReadCommand.start(context, dialog, messageCache);
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
    }
}