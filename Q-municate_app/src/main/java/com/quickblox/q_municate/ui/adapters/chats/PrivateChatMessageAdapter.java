package com.quickblox.q_municate.ui.adapters.chats;


import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.chat.model.QBAttachment;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.others.PreviewImageActivity;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.listeners.FriendOperationListener;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.qb.commands.chat.QBUpdateStatusMessageCommand;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.State;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PrivateChatMessageAdapter extends BaseChatMessagesAdapter implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = PrivateChatMessageAdapter.class.getSimpleName();

    private static int EMPTY_POSITION = -1;
    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;
    private FriendOperationListener friendOperationListener;
    private Dialog dialog;

    protected DataManager dataManager;

    public PrivateChatMessageAdapter(BaseActivity baseActivity, List<QBChatMessage> chatMessages, FriendOperationListener friendOperationListener, Dialog dialog) {
        super(baseActivity, chatMessages);
        this.friendOperationListener = friendOperationListener;
        dataManager = DataManager.getInstance();
        this.dialog = dialog;
    }

    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        Log.d(TAG, "onBindViewCustomHolder chatMessage getBody= " + chatMessage.getBody());
        CombinationMessage combinationMessage = (CombinationMessage) chatMessage;
        Log.d(TAG, "onBindViewCustomHolder combinationMessage getBody= " + combinationMessage.getBody());
        FriendsViewHolder friendsViewHolder = (FriendsViewHolder) holder;

        boolean friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                combinationMessage.getNotificationType());
        boolean friendsInfoRequestMessage = combinationMessage.getNotificationType() != null && !friendsRequestMessage;
        TextView textView = (TextView) holder.itemView.findViewById(R.id.message_textview);
        TextView timeTextMessageTextView = (TextView) holder.itemView.findViewById(R.id.time_text_message_textview);

        if (friendsRequestMessage) {
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(friendsViewHolder, View.GONE);
        } else if (friendsInfoRequestMessage) {
            Log.d(TAG, "friendsInfoRequestMessage onBindViewCustomHolderr combinationMessage getBody= " + combinationMessage.getBody());
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(friendsViewHolder, View.GONE);

            lastInfoRequestPosition = position;
        } else {
            Log.d(TAG, "else onBindViewCustomHolderr combinationMessage getBody= " + combinationMessage.getBody());
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
        }

        if (!State.READ.equals(combinationMessage.getState()) && isIncoming(chatMessage) && baseActivity.isNetworkAvailable()) {
            Log.d(TAG, "onBindViewCustomHolder QBUpdateStatusMessageCommand.start");
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(baseActivity, ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), combinationMessage, true);
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
            initListeners((FriendsViewHolder) holder, position, combinationMessage.getDialogOccupant().getUser().getUserId());
        }
    }

    @Override
    protected void onBindViewMsgRightHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        CombinationMessage combinationMessage = (CombinationMessage) chatMessage;
        ImageView view = (ImageView) holder.itemView.findViewById(R.id.custom_text_view);

        if (combinationMessage.getState() != null) {
            setMessageStatus(view, State.DELIVERED.equals(
                    combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
        } else {
            view.setImageResource(android.R.color.transparent);
        }

        super.onBindViewMsgRightHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewMsgLeftHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        CombinationMessage combinationMessage = (CombinationMessage) chatMessage;

        if (!State.READ.equals(combinationMessage.getState()) && baseActivity.isNetworkAvailable()) {
            Log.d(TAG, "onBindViewMsgLeftHolder");
            combinationMessage.setState(State.READ);
            QBUpdateStatusMessageCommand.start(baseActivity, ChatUtils.createQBDialogFromLocalDialog(dataManager, dialog), combinationMessage, true);
        }
        super.onBindViewMsgLeftHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachRightHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        resetUI(holder);
        CombinationMessage combinationMessage = (CombinationMessage) chatMessage;
        boolean ownMessage;
        ownMessage = !isIncoming(chatMessage);

        TextView attachTime = (TextView) holder.itemView.findViewById(R.id.msg_text_time_attach);
        attachTime.setText(DateUtils.formatDateSimpleTime(chatMessage.getDateSent()));

        ImageView view = (ImageView) holder.itemView.findViewById(R.id.msg_signs_attach);

        if (ownMessage && combinationMessage.getState() != null) {
            setMessageStatus(view, State.DELIVERED.equals(
                    combinationMessage.getState()), State.READ.equals(combinationMessage.getState()));
        }

        super.onBindViewAttachRightHolder(holder, chatMessage, position);
    }

    protected void onBindViewAttachLeftHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        resetUI(holder);
        TextView attachTime = (TextView) holder.itemView.findViewById(R.id.msg_text_time_attach);
        attachTime.setText(DateUtils.formatDateSimpleTime(chatMessage.getDateSent()));

        super.onBindViewAttachLeftHolder(holder, chatMessage, position);
    }

    @Override
    public long getHeaderId(int position) {
        CombinationMessage combinationMessage = (CombinationMessage) getItem(position);
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
        CombinationMessage combinationMessage = (CombinationMessage) getItem(position);
        headerTextView.setText(DateUtils.toTodayYesterdayFullMonthDate(combinationMessage.getCreatedDate()));
    }

    @Override
    public void displayAttachment(QBMessageViewHolder holder, int position) {
        QBChatMessage chatMessage = getItem(position);
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        String privateUrl = QBFile.getPrivateUrlForUID(attachment.getId());

        ImageLoader.getInstance().displayImage(privateUrl, ((ImageAttachHolder) holder).attachImageView,
                ImageLoaderUtils.UIL_DEFAULT_DISPLAY_OPTIONS, new ImageLoadingListener((ImageAttachHolder) holder),
                null);
    }

    @Override
    protected String getDate(long milliseconds) {
        return DateUtils.formatDateSimpleTime(milliseconds / 1000);
    }

    @Override
    public int getItemViewType(int position) {
        CombinationMessage combinationMessage = (CombinationMessage) getItem(position);

        if (combinationMessage.getNotificationType() != null) {
            Log.d(TAG, "combinationMessage.getNotificationType()" + combinationMessage.getNotificationType());
            return TYPE_REQUEST_MESSAGE;
        }
        return super.getItemViewType(position);
    }

    @Override
    protected QBMessageViewHolder onCreateCustomViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateCustomViewHolder viewType= " + viewType);
        return viewType == TYPE_REQUEST_MESSAGE ? new FriendsViewHolder(inflater.inflate(R.layout.item_friends_notification_message, parent, false)) : null;
    }

    public void updateList(List<QBChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
        notifyDataSetChanged();
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void findLastFriendsRequest() {
        for (int i = 0; i < getList().size(); i++) {
            findLastFriendsRequest(i, (CombinationMessage) getList().get(i));
        }
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
                        getByUserId(combinationMessage.getDialogOccupant().getUser().getUserId()) != null;
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


    public class ImageLoadingListener extends SimpleImageLoadingListener {

        private ImageAttachHolder viewHolder;
        private Bitmap loadedImageBitmap;
        private String imageUrl;

        public ImageLoadingListener(ImageAttachHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            updateUIAfterLoading();
            Log.d(TAG, "onLoadingFailed");
            imageUrl = null;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedBitmap) {
            initMaskedImageView(loadedBitmap);
            fileUtils.checkExistsFile(imageUri, loadedBitmap);
            this.imageUrl = imageUri;
        }

        private void initMaskedImageView(Bitmap loadedBitmap) {
            loadedImageBitmap = loadedBitmap;
            viewHolder.attachImageView.setOnClickListener(receiveImageFileOnClickListener());
            viewHolder.attachImageView.setImageBitmap(loadedImageBitmap);

            setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_bubble_background_attach), View.VISIBLE);
            setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_image_avatar), View.VISIBLE);

            updateUIAfterLoading();
        }

        private void updateUIAfterLoading() {
            if (viewHolder.attachmentProgressBar != null) {
                setViewVisibility(viewHolder.attachmentProgressBar, View.GONE);
            }
        }

        private View.OnClickListener receiveImageFileOnClickListener() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Log.d(TAG, "receiveImageFileOnClickListener onClick");
                    if (imageUrl != null) {
                        view.startAnimation(AnimationUtils.loadAnimation(baseActivity, R.anim.chat_attached_file_click));
                        PreviewImageActivity.start(baseActivity, imageUrl);
                    }
                }
            };
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
        @Bind(R.id.time_attach_message_textview)
        TextView timeAttachMessageTextView;

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