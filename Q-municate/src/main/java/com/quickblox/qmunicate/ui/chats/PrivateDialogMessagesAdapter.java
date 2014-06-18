package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.qmunicate.ui.views.smiles.ChatTextView;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DateUtils;

public class PrivateDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    public PrivateDialogMessagesAdapter(Context context, Cursor cursor, ScrollMessagesListener scrollMessagesListener) {
        super(context, cursor);
        this.scrollMessagesListener = scrollMessagesListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        ViewHolder viewHolder = new ViewHolder();

        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            view = layoutInflater.inflate(R.layout.list_item_dialog_own_message, null, true);
        } else {
            view = layoutInflater.inflate(R.layout.list_item_private_dialog_opponent_message, null, true);
        }

        viewHolder.progressRelativeLayout = (RelativeLayout) view.findViewById(R.id.progress_relativelayout);
        viewHolder.textMessageLinearLayout = (LinearLayout) view.findViewById(R.id.text_message_linearlayout);
        viewHolder.messageTextView = (ChatTextView) view.findViewById(R.id.message_textview);
        viewHolder.attachImageView = (ImageView) view.findViewById(R.id.attach_imageview);
        viewHolder.timeTextView = (TextView) view.findViewById(R.id.time_textview);
        viewHolder.verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
        viewHolder.verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.vertical_progressbar));
        viewHolder.centeredProgressBar =  (ProgressBar) view.findViewById(R.id.centered_progressbar);

        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        String body = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.BODY));
        String attachUrl = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.ATTACH_FILE_ID));
        long time = cursor.getLong(cursor.getColumnIndex(DialogMessageTable.Cols.TIME));
        boolean isOwnMessage = isOwnMessage(senderId);

        viewHolder.attachImageView.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(attachUrl)) {
            viewHolder.textMessageLinearLayout.setVisibility(View.GONE);
            viewHolder.progressRelativeLayout.setVisibility(View.VISIBLE);
            displayAttachImage(attachUrl, viewHolder.attachImageView, viewHolder.progressRelativeLayout, viewHolder.verticalProgressBar,
                    viewHolder.centeredProgressBar, isOwnMessage);
        } else {
            viewHolder.textMessageLinearLayout.setVisibility(View.VISIBLE);
            viewHolder.attachImageView.setVisibility(View.GONE);
            viewHolder.messageTextView.setText(body);
        }
        viewHolder.timeTextView.setText(DateUtils.longToMessageDate(time));

        boolean isRead = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.IS_READ)) > Consts.ZERO_INT_VALUE;
        if (!isRead) {
            String messageId = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.ID));
            QBUpdateStatusMessageCommand.start(context, messageId, true);
        }
    }

    private static class ViewHolder {

        RelativeLayout progressRelativeLayout;
        LinearLayout textMessageLinearLayout;
        ChatTextView messageTextView;
        ImageView attachImageView;
        TextView timeTextView;
        ProgressBar verticalProgressBar;
        ProgressBar centeredProgressBar;
    }
}