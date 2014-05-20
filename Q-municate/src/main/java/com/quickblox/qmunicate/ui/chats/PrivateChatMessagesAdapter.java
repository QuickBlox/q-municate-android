package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.ui.views.smiles.ChatTextView;
import com.quickblox.qmunicate.utils.DateUtils;

public class PrivateChatMessagesAdapter extends BaseCursorAdapter {

    private Friend opponentFriend;

    public PrivateChatMessagesAdapter(Context context, Cursor cursor, Friend opponentFriend) {
        super(context, cursor, true);
        this.opponentFriend = opponentFriend;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_item_private_chat_message, null, true);

        ViewHolder holder = new ViewHolder();

        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.avatarImageView.setOval(true);
        holder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.messageTextView = (ChatTextView) view.findViewById(R.id.message_textview);
        holder.attachImageView = (ImageView) view.findViewById(R.id.attach_imageview);
        holder.timeTextView = (TextView) view.findViewById(R.id.time_textview);
        holder.progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String senderName;
        String avatarUrl;

        String body = cursor.getString(cursor.getColumnIndex(ChatMessagesTable.Cols.BODY));
        String attachUrl = cursor.getString(cursor.getColumnIndex(ChatMessagesTable.Cols.ATTACH_FILE_URL));
        int senderId = cursor.getInt(cursor.getColumnIndex(ChatMessagesTable.Cols.SENDER_ID));
        long time = cursor.getLong(cursor.getColumnIndex(ChatMessagesTable.Cols.TIME));

        if(senderId == currentUser.getId()) {
            senderName = currentUser.getFullName();
            avatarUrl = getAvatarUrlForCurrentUser();
        } else {
            Friend senderFriend = DatabaseManager.getFriend(context, senderId);
            senderName = senderFriend.getFullname();
            avatarUrl = getAvatarUrlForFriend(opponentFriend);
        }

        if(!TextUtils.isEmpty(attachUrl)) {
            holder.messageTextView.setVisibility(View.GONE);
            holder.attachImageView.setVisibility(View.VISIBLE);
            displayAttachImage(attachUrl, holder.attachImageView, holder.progressBar);
        } else {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder.attachImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(body);
        }
        holder.nameTextView.setText(senderName);
        holder.timeTextView.setText(DateUtils.longToMessageDate(time));

        displayAvatarImage(avatarUrl, holder.avatarImageView);
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ChatTextView messageTextView;
        ImageView attachImageView;
        TextView timeTextView;
        ProgressBar progressBar;
    }
}