package com.quickblox.qmunicate.ui.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
import com.quickblox.qmunicate.utils.ChatUtils;
import com.quickblox.qmunicate.utils.Consts;

import java.util.List;

public class ChatsDialogsAdapter extends BaseListAdapter<QBDialog> {

    public ChatsDialogsAdapter(BaseActivity activity, List<QBDialog> dialogsList) {
        super(activity, dialogsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        final QBDialog data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_chat, null);
            viewHolder = createViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (data.getType() == QBDialogType.PRIVATE) {
            int occupantId = ChatUtils.getOccupantsIdsFromDialog(data).get(Consts.ZERO_VALUE);
            Friend occupant = getOccupantById(occupantId);
            viewHolder.nameTextView.setText(occupant.getFullname());
            viewHolder.userCountTextView.setVisibility(View.GONE);
        } else {
            viewHolder.nameTextView.setText(data.getName());
            viewHolder.userCountTextView.setVisibility(View.VISIBLE);
            viewHolder.userCountTextView.setText(data.getOccupants().size() + Consts.EMPTY_STRING);
        }

        if (data.getUnreadMessageCount() > Consts.ZERO_VALUE) {
            viewHolder.unreadMessagesTextView.setText(data.getUnreadMessageCount() + Consts.EMPTY_STRING);
            viewHolder.unreadMessagesTextView.setVisibility(View.VISIBLE);
        } else {
            viewHolder.unreadMessagesTextView.setVisibility(View.GONE);
        }

        viewHolder.lastMessageTextView.setText(data.getLastMessage());

        return convertView;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        viewHolder.avatarImageView.setOval(true);
        viewHolder.userCountTextView = (TextView) view.findViewById(R.id.user_count_textview);
        viewHolder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        viewHolder.lastMessageTextView = (TextView) view.findViewById(R.id.last_message_textview);
        viewHolder.unreadMessagesTextView = (TextView) view.findViewById(R.id.unread_messages_textview);
        return viewHolder;
    }

    public Friend getOccupantById(int occupantId) {
        Friend friend = DatabaseManager.getFriendById(baseActivity, occupantId);
        if (friend == null) {
            friend = new Friend();
            friend.setId(occupantId);
            friend.setFullname(occupantId + Consts.EMPTY_STRING);
        }
        return friend;
    }

    private static class ViewHolder {

        public RoundedImageView avatarImageView;
        public TextView userCountTextView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}