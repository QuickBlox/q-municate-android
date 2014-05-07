package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.ui.views.RoundedImageView;

import java.util.List;

public class ChatFriendsAdapter extends ArrayAdapter<Friend> {
    private Context context;
    private LayoutInflater layoutInflater;

    public ChatFriendsAdapter(Context context, int textViewResourceId, List<Friend> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Friend data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_chat_friend, null);
            holder = new ViewHolder();

            holder.avatarImageView = (RoundedImageView) convertView.findViewById(R.id.avatar_imageview);
            holder.avatarImageView.setOval(true);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textview);
            holder.onlineImageView = (ImageView) convertView.findViewById(R.id.online_imageview);
            holder.statusMessageTextView = (TextView) convertView.findViewById(R.id.statusMessageTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // TODO All fields
        holder.nameTextView.setText(data.getEmail());

        return convertView;
    }

    private static class ViewHolder {
        RoundedImageView avatarImageView;
        TextView nameTextView;
        ImageView onlineImageView;
        TextView statusMessageTextView;
    }
}