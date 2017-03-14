package com.quickblox.q_municate.ui.adapters.chats;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.ColorUtils;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;

import java.util.List;


public class GroupChatMessagesAdapter extends BaseChatMessagesAdapter {
    private static final String TAG = GroupChatMessagesAdapter.class.getSimpleName();
    private ColorUtils colorUtils;

    public GroupChatMessagesAdapter(BaseActivity baseActivity, List<CombinationMessage> chatMessages) {
        super(baseActivity, chatMessages);
        colorUtils = new ColorUtils();
    }

    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, CombinationMessage chatMessage, int position) {
        RequestsViewHolder viewHolder = (RequestsViewHolder) holder;
        boolean notificationMessage = chatMessage.getNotificationType() != null;

        if (notificationMessage) {
            Log.d(TAG, "onBindViewCustomHolder notificationMessage= " + (chatMessage.getBody()));
            viewHolder.messageTextView.setText(chatMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(getDate(chatMessage.getCreatedDate()));
        } else {
            Log.d(TAG, "onBindViewCustomHolder else");
        }
    }

    @Override
    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateCustomViewHolder viewType= " + viewType);
        return viewType == TYPE_REQUEST_MESSAGE ? new RequestsViewHolder(inflater.inflate(R.layout.item_notification_message, parent, false)) : null;
    }

    @Override
    protected void onBindViewMsgLeftHolder(TextMessageHolder holder, CombinationMessage chatMessage, int position) {
        holder.timeTextMessageTextView.setVisibility(View.GONE);

        String senderName;
        senderName = chatMessage.getDialogOccupant().getUser().getFullName();

        TextView opponentNameTextView = (TextView) holder.itemView.findViewById(R.id.opponent_name_text_view);
        opponentNameTextView.setTextColor(colorUtils.getRandomTextColorById(chatMessage.getDialogOccupant().getUser().getId()));
        opponentNameTextView.setText(senderName);

        TextView customMessageTimeTextView = (TextView) holder.itemView.findViewById(R.id.custom_msg_text_time_message);
        customMessageTimeTextView.setText(getDate(chatMessage.getDateSent()));

        super.onBindViewMsgLeftHolder(holder, chatMessage, position);
    }
}