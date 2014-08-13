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

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.caching.DatabaseManager;
import com.quickblox.q_municate.caching.tables.MessageTable;
import com.quickblox.q_municate.model.MessageCache;
import com.quickblox.q_municate.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate.ui.chats.emoji.EmojiTextView;
import com.quickblox.q_municate.ui.views.MaskedImageView;
import com.quickblox.q_municate.utils.DateUtils;

public class PrivateDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public PrivateDialogMessagesAdapter(Context context, Cursor cursor,
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
            view = layoutInflater.inflate(R.layout.list_item_private_message_opponent, null, true);
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

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        MessageCache messageCache = DatabaseManager.getMessageCacheFromCursor(cursor);

        boolean ownMessage = isOwnMessage(messageCache.getSenderId());

        resetUI(viewHolder);

        if (!TextUtils.isEmpty(messageCache.getAttachUrl())) {
            setDeliveryStatus(view, viewHolder, R.id.attach_message_delivery_status_imageview, ownMessage, messageCache.isDelivered());
            setViewVisibility(viewHolder.progressRelativeLayout, View.VISIBLE);
            viewHolder.timeAttachMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
            int maskedBackgroundId = getMaskedImageBackgroundId(messageCache.getSenderId());
            displayAttachImage(messageCache.getAttachUrl(), viewHolder, maskedBackgroundId);
        } else {
            setDeliveryStatus(view, viewHolder, R.id.text_message_delivery_status_imageview, ownMessage, messageCache.isDelivered());
            setViewVisibility(viewHolder.textMessageView, View.VISIBLE);
            viewHolder.messageTextView.setText(messageCache.getMessage());
            viewHolder.timeTextMessageTextView.setText(DateUtils.longToMessageDate(messageCache.getTime()));
        }

        if (ownMessage) {
            viewHolder.messageDeliveryStatusImageView.setImageResource(getMessageDeliveredIconId(messageCache.isDelivered()));
        }

        if (!messageCache.isRead()) {
            messageCache.setRead(true);
            QBUpdateStatusMessageCommand.start(context, dialog, messageCache);
        }
    }
}