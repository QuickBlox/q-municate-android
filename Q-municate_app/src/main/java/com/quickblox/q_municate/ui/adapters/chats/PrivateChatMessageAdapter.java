package com.quickblox.q_municate.ui.adapters.chats;


import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.listeners.FriendOperationListener;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.State;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PrivateChatMessageAdapter extends BaseChatMessagesAdapter implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = PrivateChatMessageAdapter.class.getSimpleName();
    private static final int SECOND_IN_MILLIS = 1000;

    private static int EMPTY_POSITION = -1;
    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;
    private FriendOperationListener friendOperationListener;

    protected DataManager dataManager;

    public PrivateChatMessageAdapter(BaseActivity baseActivity, List<CombinationMessage> chatMessages, FriendOperationListener friendOperationListener, QBChatDialog chatDialog) {
        super(baseActivity, chatDialog, chatMessages);
        this.friendOperationListener = friendOperationListener;
        dataManager = DataManager.getInstance();
        this.chatDialog = chatDialog;
    }

    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, CombinationMessage chatMessage, int position) {
        Log.d(TAG, "onBindViewCustomHolder combinationMessage getBody= " + chatMessage.getBody());
        FriendsViewHolder friendsViewHolder = (FriendsViewHolder) holder;

        boolean friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                chatMessage.getNotificationType());
        boolean friendsInfoRequestMessage = chatMessage.getNotificationType() != null && !friendsRequestMessage;
        TextView textView = (TextView) holder.itemView.findViewById(R.id.message_textview);
        TextView timeTextMessageTextView = (TextView) holder.itemView.findViewById(R.id.time_text_message_textview);

        if (friendsRequestMessage) {
            textView.setText(chatMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(chatMessage.getCreatedDate() * SECOND_IN_MILLIS));

            setVisibilityFriendsActions(friendsViewHolder, View.GONE);
        } else if (friendsInfoRequestMessage) {
            Log.d(TAG, "friendsInfoRequestMessage onBindViewCustomHolder combinationMessage getBody= " + chatMessage.getBody());
            textView.setText(chatMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(chatMessage.getCreatedDate() * SECOND_IN_MILLIS));

            setVisibilityFriendsActions(friendsViewHolder, View.GONE);

            lastInfoRequestPosition = position;
        } else {
            Log.d(TAG, "else onBindViewCustomHolderr combinationMessage getBody= " + chatMessage.getBody());
            textView.setText(chatMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(chatMessage.getCreatedDate() * SECOND_IN_MILLIS));
        }

        if (!State.READ.equals(chatMessage.getState()) && isIncoming(chatMessage) && baseActivity.isNetworkAvailable()) {
            updateMessageState(chatMessage, chatDialog);
        }
        // check if last messageCombination is request messageCombination
        boolean lastRequestMessage = (position == getItemCount() - 1 && friendsRequestMessage);
        if (lastRequestMessage) {
            findLastFriendsRequest();
        }

        // check if friend was rejected/deleted.
        if (lastRequestPosition != EMPTY_POSITION && lastRequestPosition < lastInfoRequestPosition) {
            lastRequestPosition = EMPTY_POSITION;
        } else if ((lastRequestPosition != EMPTY_POSITION && lastRequestPosition == position)) { // set visible friends actions
            setVisibilityFriendsActions((FriendsViewHolder) holder, View.VISIBLE);
            initListeners((FriendsViewHolder) holder, position, chatMessage.getDialogOccupant().getUser().getId());
        }
    }

    @Override
    protected void onBindViewMsgRightHolder(TextMessageHolder holder, CombinationMessage chatMessage, int position) {
        ImageView view = (ImageView) holder.itemView.findViewById(R.id.message_status_image_view);
        setViewVisibility(holder.avatar, View.GONE);

        if (chatMessage.getState() != null) {
            setMessageStatus(view, State.DELIVERED.equals(
                    chatMessage.getState()), State.READ.equals(chatMessage.getState()));
        } else {
            view.setImageResource(android.R.color.transparent);
        }

        super.onBindViewMsgRightHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewMsgLeftHolder(TextMessageHolder holder, CombinationMessage chatMessage, int position) {
        LinearLayout linearLayout = (LinearLayout) holder.itemView.findViewById(R.id.msg_custom_widget_frame_top);
        setViewVisibility(holder.avatar, View.GONE);
        setViewVisibility(linearLayout, View.GONE);

        updateMessageState(chatMessage, chatDialog);
        super.onBindViewMsgLeftHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachRightHolder(ImageAttachHolder holder, CombinationMessage chatMessage, int position) {
        showSendStatusView(holder, chatMessage);
        super.onBindViewAttachRightHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachRightVideoHolder(VideoAttachHolder holder, CombinationMessage chatMessage, int position) {
        showSendStatusView(holder, chatMessage);
        super.onBindViewAttachRightVideoHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachLeftVideoHolder(VideoAttachHolder holder, CombinationMessage chatMessage, int position) {
        setViewVisibility(holder.avatar, View.GONE);
        updateMessageState(chatMessage, chatDialog);
        super.onBindViewAttachLeftVideoHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachLeftHolder(ImageAttachHolder holder, CombinationMessage chatMessage, int position) {
        setViewVisibility(holder.avatar, View.GONE);
        super.onBindViewAttachLeftHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachLeftAudioHolder(AudioAttachHolder holder, CombinationMessage chatMessage, int position) {
        setViewVisibility(holder.avatar, View.GONE);
        updateMessageState(chatMessage, chatDialog);
        super.onBindViewAttachLeftAudioHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachRightAudioHolder(AudioAttachHolder holder, CombinationMessage chatMessage, int position) {
        showSendStatusView(holder, chatMessage);
        super.onBindViewAttachRightAudioHolder(holder, chatMessage, position);
    }

    private void showSendStatusView(BaseAttachHolder holder, CombinationMessage chatMessage) {
        ImageView signAttachView = holder.signAttachView;
        if (chatMessage.getState() != null) {
            setMessageStatus(signAttachView, State.DELIVERED.equals(
                    chatMessage.getState()), State.READ.equals(chatMessage.getState()));
        } else {
            signAttachView.setImageResource(android.R.color.transparent);
        }
    }

    @Override
    public long getHeaderId(int position) {
        CombinationMessage combinationMessage = getItem(position);
        return DateUtils.toShortDateLong(combinationMessage.getCreatedDate());
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = inflater.inflate(R.layout.item_chat_sticky_header_date, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        View view = holder.itemView;

        TextView headerTextView = (TextView) view.findViewById(R.id.header_date_textview);
        CombinationMessage combinationMessage = getItem(position);
        headerTextView.setText(DateUtils.toTodayYesterdayFullMonthDate(combinationMessage.getCreatedDate()));
    }

    @Override
    public int getItemViewType(int position) {
        CombinationMessage combinationMessage = getItem(position);

        if (combinationMessage.getNotificationType() != null) {
            Log.d(TAG, "combinationMessage.getNotificationType()" + combinationMessage.getNotificationType());
            return TYPE_REQUEST_MESSAGE;
        }
        return super.getItemViewType(position);
    }

    @Override
    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        return viewType == TYPE_REQUEST_MESSAGE ? new FriendsViewHolder(inflater.inflate(R.layout.item_friends_notification_message, parent, false)) : null;
    }

    @Override
    protected void showAttachUI(ImageAttachHolder viewHolder, boolean isIncoming) {
        setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_bubble_background), View.VISIBLE);
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void findLastFriendsRequest() {
        for (int i = 0; i < getList().size(); i++) {
            findLastFriendsRequest(i, getList().get(i));
        }
    }

    public void clearLastRequestMessagePosition() {
        lastRequestPosition = EMPTY_POSITION;
    }

    private void findLastFriendsRequest(int position, CombinationMessage combinationMessage) {
        boolean ownMessage;
        boolean friendsRequestMessage;
        boolean isFriend;

        if (combinationMessage.getNotificationType() != null) {
            ownMessage = !combinationMessage.isIncoming(currentUser.getId());
            friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                    combinationMessage.getNotificationType());

            if (friendsRequestMessage && !ownMessage) {
                isFriend = dataManager.getFriendDataManager().
                        getByUserId(combinationMessage.getDialogOccupant().getUser().getId()) != null;
                if (!isFriend) {
                    lastRequestPosition = position;
                }
            }
        }
    }

    private void setVisibilityFriendsActions(FriendsViewHolder viewHolder, int visibility) {
        setViewVisibility(viewHolder.acceptFriendImageView, visibility);
        setViewVisibility(viewHolder.dividerView, visibility);
        setViewVisibility(viewHolder.rejectFriendImageView, visibility);
    }

    private void initListeners(FriendsViewHolder viewHolder, final int position, final int userId) {
        viewHolder.acceptFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onAcceptUserClicked(position, userId);
            }
        });

        viewHolder.rejectFriendImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendOperationListener.onRejectUserClicked(position, userId);
            }
        });
    }

    private class FindLastFriendsRequestThread extends Thread {

        @Override
        public void run() {
            findLastFriendsRequest();
        }
    }

    protected void setMessageStatus(ImageView imageView, boolean messageDelivered, boolean messageRead) {
        imageView.setImageResource(getMessageStatusIconId(messageDelivered, messageRead));
    }

    protected int getMessageStatusIconId(boolean isDelivered, boolean isRead) {
        int iconResourceId = 0;

        if (isRead) {
            iconResourceId = R.drawable.ic_status_mes_sent_received;
        } else if (isDelivered) {
            iconResourceId = R.drawable.ic_status_mes_sent;
        }

        return iconResourceId;
    }

    protected static class FriendsViewHolder extends QBMessageViewHolder {
        @Nullable
        @Bind(R.id.message_textview)
        TextView messageTextView;

        @Nullable
        @Bind(R.id.time_text_message_textview)
        TextView timeTextMessageTextView;

        @Nullable
        @Bind(R.id.accept_friend_imagebutton)
        ImageView acceptFriendImageView;

        @Nullable
        @Bind(R.id.divider_view)
        View dividerView;

        @Nullable
        @Bind(R.id.reject_friend_imagebutton)
        ImageView rejectFriendImageView;


        public FriendsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }
}