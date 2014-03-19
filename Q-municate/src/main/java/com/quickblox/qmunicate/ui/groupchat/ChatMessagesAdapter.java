package com.quickblox.qmunicate.ui.groupchat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.ChatMessage;

import java.util.List;

public class ChatMessagesAdapter extends ArrayAdapter<ChatMessage> {
    private Context context;
    private LayoutInflater layoutInflater;

    public ChatMessagesAdapter(Context context, int textViewResourceId, List<ChatMessage> list) {
        super(context, textViewResourceId, list);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage data = getItem(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_chat_message, null);
            holder = new ViewHolder();

            holder.avatarImageView = (ImageView) convertView.findViewById(R.id.avatarImageView);
            holder.nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            holder.messageTextView = (TextView) convertView.findViewById(R.id.messageTextView);
            holder.timeTextView = (TextView) convertView.findViewById(R.id.timeTextView);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // TODO All fields

        return convertView;
    }

    private static class ViewHolder {
        ImageView avatarImageView;
        TextView nameTextView;
        TextView messageTextView;
        TextView timeTextView;
    }
}