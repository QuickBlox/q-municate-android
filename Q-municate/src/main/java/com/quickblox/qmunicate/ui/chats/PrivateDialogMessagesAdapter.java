package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.ui.views.smiles.ChatTextView;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;

public class PrivateDialogMessagesAdapter extends BaseCursorAdapter {

    private Friend opponentFriend;
    private QBDialog dialog;

    public PrivateDialogMessagesAdapter(Context context, Cursor cursor, Friend opponentFriend, QBDialog dialog) {
        super(context, cursor, true);
        this.opponentFriend = opponentFriend;
        this.dialog = dialog;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            view = layoutInflater.inflate(R.layout.list_item_dialog_message_left, null, true);
        } else {
            view = layoutInflater.inflate(R.layout.list_item_dialog_message_right, null, true);
        }

        ViewHolder holder = new ViewHolder();

        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.avatarImageView.setOval(true);
        holder.messageTextView = (ChatTextView) view.findViewById(R.id.message_textview);
        holder.attachImageView = (ImageView) view.findViewById(R.id.attach_imageview);
        holder.timeTextView = (TextView) view.findViewById(R.id.time_textview);
        holder.progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        holder.pleaseWaitTextView = (TextView) view.findViewById(R.id.please_wait_textview);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        String avatarUrl;

        String body = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.BODY));
        String attachUrl = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.ATTACH_FILE_ID));
        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        long time = cursor.getLong(cursor.getColumnIndex(DialogMessageTable.Cols.TIME));

        viewHolder.attachImageView.setVisibility(View.GONE);

        if (isOwnMessage(senderId)) {
            avatarUrl = getAvatarUrlForCurrentUser();
        } else {
            avatarUrl = getAvatarUrlForFriend(opponentFriend);
        }

        if (!TextUtils.isEmpty(attachUrl)) {
            viewHolder.messageTextView.setVisibility(View.GONE);
            displayAttachImage(attachUrl, viewHolder.pleaseWaitTextView, viewHolder.attachImageView,
                    viewHolder.progressBar);
        } else {
            viewHolder.messageTextView.setVisibility(View.VISIBLE);
            viewHolder.attachImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setText(body);
        }
        viewHolder.timeTextView.setText(DateUtils.longToMessageDate(time));

        if(dialog != null) {
            boolean isRead = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.IS_READ)) > 0;
            if(!isRead) {
                String messageId = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.ID));
                QBUpdateStatusMessageCommand.start(context, messageId, true);
            }
        }

        displayAvatarImage(avatarUrl, viewHolder.avatarImageView);
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return Consts.MESSAGES_TYPE_COUNT;
    }

    private boolean isOwnMessage(int senderId) {
        return senderId == currentUser.getId();
    }

    private void displayAttachImage(String uri, final TextView pleaseWaitTextView,
                                    final ImageView attachImageView, final ProgressBar progressBar) {
        ImageLoader.getInstance().loadImage(uri, new SimpleImageLoading(pleaseWaitTextView, attachImageView,
                progressBar));
    }

    private int getItemViewType(Cursor cursor) {
        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            return Consts.LEFT_CHAT_MESSAGE_TYPE_1;
        } else {
            return Consts.RIGHT_CHAT_MESSAGE_TYPE_2;
        }
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        ChatTextView messageTextView;
        ImageView attachImageView;
        TextView timeTextView;
        ProgressBar progressBar;
        TextView pleaseWaitTextView;
    }
}