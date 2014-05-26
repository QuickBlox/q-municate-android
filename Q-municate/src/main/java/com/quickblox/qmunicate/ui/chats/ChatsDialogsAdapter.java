package com.quickblox.qmunicate.ui.chats;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.module.chat.model.QBDialogType;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;
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
            int occupantId = getOccupantIdFromPrivateDialog(data);
            Friend occupant = getOccupantById(occupantId);
            viewHolder.nameTextView.setText(occupant.getFullname());
        } else {
            viewHolder.nameTextView.setText(data.getName());
        }

        viewHolder.lastMessageTextView.setText(data.getLastMessage());

        return convertView;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.avatarImageView.setOval(true);
        holder.userCountTextView = (TextView) view.findViewById(R.id.user_count_textview);
        holder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.lastMessageTextView = (TextView) view.findViewById(R.id.last_message_textview);
        holder.unreadMessagesTextView = (TextView) view.findViewById(R.id.unread_messages_textview);
        return holder;
    }

    public int getOccupantIdFromPrivateDialog(QBDialog dialog) {
        int occupantId = Consts.ZERO_VALUE;
        QBUser user = App.getInstance().getUser();
        List<Integer> occupantsList = dialog.getOccupants();
        for (int occupant : occupantsList) {
            if (occupant != user.getId()) {
                occupantId = occupant;
            }
        }
        return occupantId;
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