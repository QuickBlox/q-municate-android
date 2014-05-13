package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.PrivateChatMessagesTable;
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
        holder.timeTextView = (TextView) view.findViewById(R.id.time_textview);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String body = cursor.getString(cursor.getColumnIndex(PrivateChatMessagesTable.Cols.BODY));
        int senderId = cursor.getInt(cursor.getColumnIndex(PrivateChatMessagesTable.Cols.SENDER_ID));
        Cursor senderCursor = DatabaseManager.getCursorFriendById(context, senderId);
        Friend senderFriend = DatabaseManager.getFriendFromCursor(senderCursor);
        long time = cursor.getLong(cursor.getColumnIndex(PrivateChatMessagesTable.Cols.TIME));

        holder.messageTextView.setText(body);
        holder.nameTextView.setText(senderFriend.getFullname());
        holder.timeTextView.setText(DateUtils.longToMessageDate(time));

        String avatarUrl = getAvatarUrl(senderId);
        displayImage(avatarUrl, holder.avatarImageView);
    }

    private String getAvatarUrl(int friendId) {
        if (currentUser.getId() == friendId) {
            return getAvatarUrlForCurrentUser();
        } else {
            return getAvatarUrlForFriend(opponentFriend);
        }
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ChatTextView messageTextView;
        TextView timeTextView;
    }
}