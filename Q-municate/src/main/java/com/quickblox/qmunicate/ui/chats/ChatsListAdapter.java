package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Chat;

import java.util.List;

public class ChatsListAdapter extends ArrayAdapter<Chat> {
    private Context context;
    private LayoutInflater layoutInflater;

    public ChatsListAdapter(Context context, int textViewResourceId, List<Chat> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Chat data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_chat, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        // TODO IS add image loading
        holder.nameTextView.setText(data.getName());
        // TODO IS add badges
        // TODO IS set placeholders

        return convertView;
    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatar_imageview);
        holder.userCountTextView = (TextView) view.findViewById(R.id.userCountTextView);
        holder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
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
