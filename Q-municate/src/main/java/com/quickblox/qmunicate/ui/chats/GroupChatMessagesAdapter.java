package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.ui.views.smiles.ChatTextView;
import com.quickblox.qmunicate.utils.DateUtils;

public class GroupChatMessagesAdapter extends BaseCursorAdapter {

    public GroupChatMessagesAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_item_chat_message_left, null, true);

        ViewHolder holder = new ViewHolder();

        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.avatarImageView.setOval(true);
        holder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.messageTextView = (ChatTextView) view.findViewById(R.id.message_textview);
        holder.timeTextView = (TextView) view.findViewById(R.id.time_textview);
        holder.nameTextView.setVisibility(View.VISIBLE);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String senderName;
        String avatarUrl;

        String body = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.BODY));
        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));

        if(senderId == currentUser.getId()) {
            senderName = currentUser.getFullName();
            avatarUrl = getAvatarUrlForCurrentUser();
        } else {
            Friend senderFriend = DatabaseManager.getFriendById(context, senderId);
            senderName = senderFriend.getFullname();
            avatarUrl = getAvatarUrlForFriend(senderFriend);
        }

        long time = cursor.getLong(cursor.getColumnIndex(DialogMessageTable.Cols.TIME));

        holder.messageTextView.setText(body);
        holder.nameTextView.setText(senderName);
        holder.timeTextView.setText(DateUtils.longToMessageDate(time));

        displayAvatarImage(avatarUrl, holder.avatarImageView);
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ChatTextView messageTextView;
        TextView timeTextView;
    }
}