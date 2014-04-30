package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.tables.PrivateChatMessagesTable;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.utils.DateUtils;

public class PrivateChatMessagesAdapter extends BaseCursorAdapter {

    public PrivateChatMessagesAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.list_item_private_chat_message, null, true);

        ViewHolder holder = new ViewHolder();

        holder.avatarImageView = (ImageView) view.findViewById(R.id.avatarImageView);
        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        holder.timeTextView = (TextView) view.findViewById(R.id.timeTextView);

        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();

        String subject = cursor.getString(cursor.getColumnIndex(PrivateChatMessagesTable.Cols.SUBJECT));
        String body = cursor.getString(cursor.getColumnIndex(PrivateChatMessagesTable.Cols.BODY));
        String senderName = cursor.getString(cursor.getColumnIndex(
                PrivateChatMessagesTable.Cols.SENDER_NAME));
        long time = cursor.getLong(cursor.getColumnIndex(PrivateChatMessagesTable.Cols.TIME));

        holder.messageTextView.setText(body);
        holder.nameTextView.setText(senderName);
        holder.timeTextView.setText(DateUtils.longToMessageDate(time));

//        Animation animation = AnimationUtils.loadAnimation(context, R.anim.message_in_animation);
//        view.startAnimation(animation);
    }

    private static class ViewHolder {

        ImageView avatarImageView;
        TextView nameTextView;
        TextView messageTextView;
        TextView timeTextView;
    }
}