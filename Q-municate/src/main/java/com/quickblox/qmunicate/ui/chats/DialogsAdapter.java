package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

public class DialogsAdapter extends BaseCursorAdapter {

    public DialogsAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View convertView;
        convertView = layoutInflater.inflate(R.layout.list_item_dialog, null);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
        viewHolder.avatarImageView.setOval(true);
        viewHolder.userCountTextView = (TextView) convertView.findViewById(R.id.user_count_textview);
        viewHolder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
        viewHolder.lastMessageTextView = (TextView) convertView.findViewById(R.id.last_message_textview);
        viewHolder.unreadMessagesTextView = (TextView) convertView.findViewById(
                R.id.unread_messages_textview);
        convertView.setTag(viewHolder);
        return convertView;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        QBDialog dialog = DatabaseManager.getDialogFromCursor(cursor);

        if (dialog.getType().equals(QBDialogType.PRIVATE)) {
            int occupantId = ChatUtils.getOccupantIdFromList(dialog.getOccupants());
            Friend occupant = getOccupantById(occupantId);
            viewHolder.nameTextView.setText(occupant.getFullname());
            viewHolder.userCountTextView.setVisibility(View.GONE);
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_user);
        } else {
            viewHolder.nameTextView.setText(dialog.getName());
            viewHolder.userCountTextView.setVisibility(View.VISIBLE);
            viewHolder.userCountTextView.setText(dialog.getOccupants().size() + Consts.EMPTY_STRING);
            viewHolder.avatarImageView.setImageResource(R.drawable.placeholder_group);
        }

        if (dialog.getUnreadMessageCount() > Consts.ZERO_INT_VALUE) {
            viewHolder.unreadMessagesTextView.setText(dialog.getUnreadMessageCount() + Consts.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        viewHolder.lastMessageTextView.setText(dialog.getLastMessage());
    }

    public Friend getOccupantById(int occupantId) {
        Friend friend = DatabaseManager.getFriendById(context, occupantId);
        if (friend == null) {
            friend = new Friend();
            friend.setId(occupantId);
            friend.setFullname(occupantId + Consts.EMPTY_STRING);
        }
        return friend;
    }

    @Override
    protected void onContentChanged() {
        super.onContentChanged();
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView userCountTextView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}