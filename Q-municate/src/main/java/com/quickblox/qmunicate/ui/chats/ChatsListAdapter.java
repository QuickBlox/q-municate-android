package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.DatabaseManager;
import com.quickblox.qmunicate.model.Chat;
import com.quickblox.qmunicate.model.PrivateChat;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.RoundedImageView;

import java.util.List;

public class ChatsListAdapter extends BaseCursorAdapter {
    private Context context;
    private LayoutInflater layoutInflater;

    public ChatsListAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

//    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder;
//        Chat data = getItem(position);
//
//        if (convertView == null) {
//            convertView = layoutInflater.inflate(R.layout.list_item_chat, null);
//            holder = createViewHolder(convertView);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        // TODO IS add image loading
//        holder.nameTextView.setText(data.getName());
//        // TODO IS add badges
//        // TODO IS set placeholders
//
//        return convertView;
//    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder;
        View view = layoutInflater.inflate(R.layout.list_item_chat, null);
        holder = createViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        PrivateChat data = DatabaseManager.getPrivateChatFromCursor(cursor);
        holder.nameTextView.setText(data.getName());
        holder.lastMessageTextView.setText(data.getLastMessage().getBody());

    }

    private ViewHolder createViewHolder(View view) {
        ViewHolder holder = new ViewHolder();
        holder.avatarImageView = (RoundedImageView) view.findViewById(R.id.avatar_imageview);
        holder.avatarImageView.setOval(true);
        holder.userCountTextView = (TextView) view.findViewById(R.id.userCountTextView);
        holder.nameTextView = (TextView) view.findViewById(R.id.name_textview);
        holder.lastMessageTextView = (TextView) view.findViewById(R.id.lastMessageTextView);
        holder.unreadMessagesTextView = (TextView) view.findViewById(R.id.unreadMessagesTextView);
        return holder;
    }

    private static class ViewHolder {
        public RoundedImageView avatarImageView;
        public TextView userCountTextView;
        public TextView nameTextView;
        public TextView lastMessageTextView;
        public TextView unreadMessagesTextView;
    }
}