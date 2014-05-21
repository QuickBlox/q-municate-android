package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.caching.tables.ChatMessagesTable;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.ui.views.smiles.ChatTextView;
import com.quickblox.qmunicate.utils.DateUtils;

import java.util.List;

public class GroupChatMessagesAdapter extends BaseCursorAdapter {

    private List<Friend> opponentFriends;

    public GroupChatMessagesAdapter(Context context, Cursor cursor, List<Friend> opponentFriends) {
        super(context, cursor, true);
        this.opponentFriends = opponentFriends;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_item_private_chat_message_right, null, true);

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
        String senderName;
        String avatarUrl;

        String body = cursor.getString(cursor.getColumnIndex(ChatMessagesTable.Cols.BODY));
        int senderId = cursor.getInt(cursor.getColumnIndex(ChatMessagesTable.Cols.SENDER_ID));

        if(senderId == currentUser.getId()) {
            Log.i("Sender", "currentUser");
            senderName = currentUser.getFullName();
//            avatarUrl = getAvatarUrlForCurrentUser();
        } else {
            Log.i("Sender", "otherUser");
            Cursor senderCursor = DatabaseManager.getCursorFriendById(context, senderId);
            Friend senderFriend = DatabaseManager.getFriendFromCursor(senderCursor);
            senderName = senderFriend.getFullname();
//            avatarUrl = getAvatarUrlForFriend(opponentFriends);
        }

        long time = cursor.getLong(cursor.getColumnIndex(ChatMessagesTable.Cols.TIME));

        holder.messageTextView.setText(body);
        holder.nameTextView.setText(senderName);
        holder.timeTextView.setText(DateUtils.longToMessageDate(time));

//        displayImage(avatarUrl, holder.avatarImageView);
    }

    private static class ViewHolder {

        RoundedImageView avatarImageView;
        TextView nameTextView;
        ChatTextView messageTextView;
        TextView timeTextView;
    }
}