package com.quickblox.q_municate.ui.chats;

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

import com.quickblox.module.chat.model.QBDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.caching.tables.DialogMessageTable;
import com.quickblox.q_municate.qb.commands.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate.ui.views.smiles.ChatTextView;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.DateUtils;

public class PrivateDialogMessagesAdapter extends BaseDialogMessagesAdapter {

    private RelativeLayout progressRelativeLayout;
    private RelativeLayout attachMessageRelativeLayout;
    private LinearLayout textMessageLinearLayout;
    private ChatTextView messageTextView;
    private ImageView attachImageView;
    private TextView timeTextMessageTextView;
    private TextView timeAttachMessageTextView;
    private ProgressBar verticalProgressBar;
    private ProgressBar centeredProgressBar;

    public PrivateDialogMessagesAdapter(Context context, Cursor cursor, ScrollMessagesListener scrollMessagesListener, QBDialog dialog) {
        super(context, cursor);
        this.scrollMessagesListener = scrollMessagesListener;
        this.dialog = dialog;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;

        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            view = layoutInflater.inflate(R.layout.list_item_dialog_own_message, null, true);
        } else {
            view = layoutInflater.inflate(R.layout.list_item_private_dialog_opponent_message, null, true);
        }

        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        attachMessageRelativeLayout = (RelativeLayout) view.findViewById(R.id.attach_message_relativelayout);
        timeAttachMessageTextView = (TextView) view.findViewById(R.id.time_attach_message_textview);
        progressRelativeLayout = (RelativeLayout) view.findViewById(R.id.progress_relativelayout);
        textMessageLinearLayout = (LinearLayout) view.findViewById(R.id.text_message_view);
        messageTextView = (ChatTextView) view.findViewById(R.id.message_textview);
        attachImageView = (ImageView) view.findViewById(R.id.attach_imageview);
        timeTextMessageTextView = (TextView) view.findViewById(R.id.time_text_message_textview);
        verticalProgressBar = (ProgressBar) view.findViewById(R.id.vertical_progressbar);
        verticalProgressBar.setProgressDrawable(context.getResources().getDrawable(R.drawable.vertical_progressbar));
        centeredProgressBar =  (ProgressBar) view.findViewById(R.id.centered_progressbar);

        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        String body = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.BODY));
        String attachUrl = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.ATTACH_FILE_ID));
        long time = cursor.getLong(cursor.getColumnIndex(DialogMessageTable.Cols.TIME));
        boolean isOwnMessage = isOwnMessage(senderId);

        attachMessageRelativeLayout.setVisibility(View.GONE);

        if (!TextUtils.isEmpty(attachUrl)) {
            timeAttachMessageTextView.setText(DateUtils.longToMessageDate(time));
            textMessageLinearLayout.setVisibility(View.GONE);
            progressRelativeLayout.setVisibility(View.VISIBLE);
            displayAttachImage(attachUrl, attachImageView, progressRelativeLayout,
                    attachMessageRelativeLayout, verticalProgressBar,
                    centeredProgressBar, isOwnMessage);
        } else {
            timeTextMessageTextView.setText(DateUtils.longToMessageDate(time));
            textMessageLinearLayout.setVisibility(View.VISIBLE);
            attachImageView.setVisibility(View.GONE);
            messageTextView.setText(body);
        }

        boolean isRead = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.IS_READ)) > Consts.ZERO_INT_VALUE;
        if (!isRead) {
            String messageId = cursor.getString(cursor.getColumnIndex(DialogMessageTable.Cols.ID));
            QBUpdateStatusMessageCommand.start(context, dialog, messageId, time, true);
        }
    }
}