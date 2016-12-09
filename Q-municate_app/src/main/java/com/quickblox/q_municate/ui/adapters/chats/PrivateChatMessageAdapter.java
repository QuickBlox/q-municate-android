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
import com.quickblox.q_municate.utils.FileUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.DialogNotification;
import com.quickblox.q_municate_db.models.State;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PrivateChatMessageAdapter extends QBMessagesAdapter implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = PrivateChatMessageAdapter.class.getSimpleName();
    protected static final int TYPE_REQUEST_MESSAGE = 5;

    private static int EMPTY_POSITION = -1;
    private int lastRequestPosition = EMPTY_POSITION;
    private int lastInfoRequestPosition = EMPTY_POSITION;
    private FileUtils fileUtils;

    private List<CombinationMessage> combinationMessagesList;
    protected final BaseActivity baseActivity;
    protected QBUser currentUser;

    protected DataManager dataManager;

    public PrivateChatMessageAdapter(BaseActivity baseActivity, List<QBChatMessage> chatMessages, List<CombinationMessage> combinationMessagesList) {
        super(baseActivity.getApplicationContext(), chatMessages);
        dataManager = DataManager.getInstance();
        currentUser = AppSession.getSession().getUser();
        this.combinationMessagesList = combinationMessagesList;
        fileUtils = new FileUtils();
        this.baseActivity = baseActivity;
    }

    private CombinationMessage getCombinationMessage(int position) {
        return combinationMessagesList.get(position);
    }

    @Override
    protected void onBindViewCustomHolder(QBMessageViewHolder holder, QBChatMessage chatMessage, int position) {
        Log.d(TAG, "onBindViewCustomHolderr chatMessage getBody= " + chatMessage.getBody());
        FriendsViewHolder friendsViewHolder = (FriendsViewHolder) holder;

        CombinationMessage combinationMessage = getCombinationMessage(position);
        boolean friendsRequestMessage = DialogNotification.Type.FRIENDS_REQUEST.equals(
                combinationMessage.getNotificationType());
        boolean friendsInfoRequestMessage = combinationMessage
                .getNotificationType() != null && !friendsRequestMessage;
        TextView textView = (TextView) holder.itemView.findViewById(R.id.message_textview);
        TextView timeTextMessageTextView = (TextView) holder.itemView.findViewById(R.id.time_text_message_textview);

        if (friendsRequestMessage) {
            Log.d(TAG, "friendsRequestMessage onBindViewCustomHolderr chatMessage getBody= " + combinationMessage.getBody());
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(friendsViewHolder, View.GONE);
        } else if (friendsInfoRequestMessage) {
            Log.d(TAG, "friendsInfoRequestMessage onBindViewCustomHolderr chatMessage getBody= " + combinationMessage.getBody());
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));

            setVisibilityFriendsActions(friendsViewHolder, View.GONE);

            lastInfoRequestPosition = position;
        } else {
            Log.d(TAG, "else onBindViewCustomHolderr chatMessage getBody= " + combinationMessage.getBody());
            textView.setText(combinationMessage.getBody());
            timeTextMessageTextView.setText(DateUtils.formatDateSimpleTime(combinationMessage.getCreatedDate()));
        }
    }

    @Override
    protected void onBindViewMsgRightHolder(TextMessageHolder holder, QBChatMessage chatMessage, int position) {
        boolean ownMessage;
        ownMessage = !isIncoming(chatMessage);
        ImageView view = (ImageView) holder.itemView.findViewById(R.id.custom_text_view);

        CombinationMessage message = getCombinationMessage(position);

        if (ownMessage && message != null && message.getState() != null) {
            setMessageStatus(view, State.DELIVERED.equals(
                    message.getState()), State.READ.equals(message.getState()));
        } else if (ownMessage && message != null && message.getState() == null) {
            view.setImageResource(android.R.color.transparent);
        }

        super.onBindViewMsgRightHolder(holder, chatMessage, position);
    }

    @Override
    protected void onBindViewAttachRightHolder(ImageAttachHolder holder, QBChatMessage chatMessage, int position) {
        resetUI(holder);
        boolean ownMessage;
        ownMessage = !isIncoming(chatMessage);
        CombinationMessage combinationMessage = getCombinationMessage(position);

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
        QBChatMessage chatMessage = getItem(position);
        return DateUtils.toShortDateLong(chatMessage.getDateSent());
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
        QBChatMessage chatMessages = getItem(position);
        headerTextView.setText(DateUtils.toTodayYesterdayFullMonthDate(chatMessages.getDateSent()));
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
        CombinationMessage combinationMessage = getCombinationMessage(position);
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

    public void updateList(List<QBChatMessage> newData) {
        chatMessages = newData;
        notifyDataSetChanged();
    }

    public void findLastFriendsRequestMessagesPosition() {
        new FindLastFriendsRequestThread().run();
    }

    private void findLastFriendsRequest() {
        for (int i = 0; i < combinationMessagesList.size(); i++) {
            findLastFriendsRequest(i, combinationMessagesList.get(i));
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

    public void setList(List<CombinationMessage> combinationMessagesList) {
        this.combinationMessagesList = combinationMessagesList;
    }

    private void setVisibilityFriendsActions(FriendsViewHolder viewHolder, int visibility) {
        setViewVisibility(viewHolder.acceptFriendImageView, visibility);
        setViewVisibility(viewHolder.dividerView, visibility);
        setViewVisibility(viewHolder.rejectFriendImageView, visibility);
    }

    protected void resetUI(ImageAttachHolder viewHolder) {
        setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_bubble_background_attach), View.GONE);
        setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_image_avatar), View.GONE);
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

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
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