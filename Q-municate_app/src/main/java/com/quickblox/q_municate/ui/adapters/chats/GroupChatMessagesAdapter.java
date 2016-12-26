package com.quickblox.q_municate.ui.adapters.chats;

import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.ColorUtils;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_db.models.Dialog;

import java.util.List;


public class GroupChatMessagesAdapter extends BaseChatMessagesAdapter {
    private static final String TAG = GroupChatMessagesAdapter.class.getSimpleName();
    private ColorUtils colorUtils;

    public GroupChatMessagesAdapter(BaseActivity baseActivity, List<QBChatMessage> chatMessages, Dialog dialog) {
        super(baseActivity, chatMessages);
        this.dialog = dialog;
        colorUtils = new ColorUtils();
    }

    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        RequestsViewHolder viewHolder = (RequestsViewHolder) holder;
        CombinationMessage combinationMessage = (CombinationMessage) chatMessage;
        boolean notificationMessage = combinationMessage.getNotificationType() != null;

        if (notificationMessage) {
            Log.d(TAG, "onBindViewCustomHolder notificationMessage= " + (combinationMessage.getBody()));
            viewHolder.messageTextView.setText(combinationMessage.getBody());
            viewHolder.timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
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
    protected void onBindViewMsgLeftHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        CombinationMessage combinationMessage = (CombinationMessage) chatMessage;
        String senderName;
        senderName = combinationMessage.getDialogOccupant().getUser().getFullName();
        TextView textView = (TextView) holder.itemView.findViewById(R.id.custom_text_view);

        textView.setTextColor(colorUtils.getRandomTextColorById(combinationMessage.getDialogOccupant().getUser().getUserId()));
        textView.setText(senderName);

        super.onBindViewMsgLeftHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachRightHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        resetUI(holder);

        TextView attachTime = (TextView) holder.itemView.findViewById(R.id.msg_text_time_attach);
        attachTime.setText(DateUtils.formatDateSimpleTime(chatMessage.getDateSent()));

        super.onBindViewAttachRightHolder(holder, chatMessage, position);
    }

    protected void onBindViewAttachLeftHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        resetUI(holder);
        TextView attachTime = (TextView) holder.itemView.findViewById(R.id.msg_text_time_attach);
        attachTime.setText(DateUtils.formatDateSimpleTime(chatMessage.getDateSent()));

        super.onBindViewAttachLeftHolder(holder, chatMessage, position);
    }
}
