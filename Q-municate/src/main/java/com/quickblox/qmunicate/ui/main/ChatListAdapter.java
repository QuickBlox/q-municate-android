package com.quickblox.qmunicate.ui.main;

import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Chat;
import com.quickblox.qmunicate.ui.base.BaseListAdapter;

import java.util.List;

public class ChatListAdapter extends BaseListAdapter<Chat> {

    private final LayoutInflater inflater;

    public ChatListAdapter(FragmentActivity activity, List<Chat> objects) {
        super(activity, objects);
        inflater = LayoutInflater.from(activity);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Chat chat = objects.get(position);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item_chat, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // TODO add image loading
        holder.nameTextView.setText(chat.getName());
        // TODO add badges
        // TODO set placeholders

        return convertView;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatarImageView);
        holder.userCountTextView = (TextView) view.findViewById(R.id.userCountTextView);
        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.lastMessageTextView = (TextView) view.findViewById(R.id.lastMessageTextView);
        holder.unreadMessagesTextView = (TextView) view.findViewById(R.id.unreadMessagesTextView);
        return holder;
    }

    private static class ViewHolder {
        public ImageView avatarImageView;
        public TextView userCountTextView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}
