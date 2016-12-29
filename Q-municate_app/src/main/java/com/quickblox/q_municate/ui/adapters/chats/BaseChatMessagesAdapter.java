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
import com.quickblox.content.model.QBFile;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.others.PreviewImageActivity;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.FileUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.ui.kit.chatmessage.adapter.QBMessagesAdapter;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.Collection;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class BaseChatMessagesAdapter extends QBMessagesAdapter<CombinationMessage> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private static final String TAG = BaseChatMessagesAdapter.class.getSimpleName();
    protected static final int TYPE_REQUEST_MESSAGE = 5;
    protected Dialog dialog;
    protected QBUser currentUser;
    protected final BaseActivity baseActivity;

    protected FileUtils fileUtils;

    BaseChatMessagesAdapter(BaseActivity baseActivity, List<CombinationMessage> chatMessages) {
        super(baseActivity.getApplicationContext(), chatMessages);
        this.baseActivity = baseActivity;
        currentUser = AppSession.getSession().getUser();
        fileUtils = new FileUtils();
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
            Log.d(TAG, "getItemViewType TYPE_REQUEST_MESSAGE");
            return TYPE_REQUEST_MESSAGE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public void displayAttachment(QBMessageViewHolder holder, int position) {
        CombinationMessage chatMessage = getItem(position);
        Collection<QBAttachment> attachments = chatMessage.getAttachments();
        QBAttachment attachment = attachments.iterator().next();
        String privateUrl = QBFile.getPrivateUrlForUID(attachment.getId());

        ImageLoader.getInstance().displayImage(privateUrl, ((ImageAttachHolder) holder).attachImageView,
                ImageLoaderUtils.UIL_DEFAULT_DISPLAY_OPTIONS, new ImageLoadingListener((ImageAttachHolder) holder, isIncoming(chatMessage)),
                null);
    }


    @Override
    public void displayAvatarImage(String url, ImageView imageView) {
        ImageLoader.getInstance().displayImage(url, imageView, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS);
    }

    @Override
    public String obtainAvatarUrl(int valueType, CombinationMessage chatMessage) {
        return chatMessage.getDialogOccupant().getUser().getAvatar();
    }

    public void updateList(List<CombinationMessage> chatMessages) {
        addList(chatMessages);
    }

    @Override
    protected String getDate(long milliseconds) {
        return DateUtils.formatDateSimpleTime(milliseconds / 1000);
    }

    private void resetAttachUI(ImageAttachHolder viewHolder) {
        setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_bubble_background_attach), View.GONE);
        setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_image_avatar), View.GONE);
    }

    protected void showAttachUI(ImageAttachHolder viewHolder, boolean isIncoming) {
        if (isIncoming){
            setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_image_avatar), View.VISIBLE);
        }
        setViewVisibility(viewHolder.itemView.findViewById(R.id.msg_bubble_background_attach), View.VISIBLE);
    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public boolean isEmpty() {
        return chatMessages.size() == 0;
    }

    @Override
    protected boolean isIncoming(CombinationMessage chatMessage) {
        return chatMessage.isIncoming(currentUser.getId());
    }

    public class ImageLoadingListener extends SimpleImageLoadingListener {

        private ImageAttachHolder viewHolder;
        private Bitmap loadedImageBitmap;
        private String imageUrl;
        private boolean isIncoming;

        public ImageLoadingListener(ImageAttachHolder viewHolder, boolean isIncoming) {
            this.viewHolder = viewHolder;
            this.isIncoming = isIncoming;
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            updateUIAfterLoading();
            resetAttachUI(viewHolder);
            Log.d(TAG, "onLoadingFailed");
            imageUrl = null;
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedBitmap) {
            initMaskedImageView(loadedBitmap);
            fileUtils.checkExistsFile(imageUri, loadedBitmap);
            this.imageUrl = imageUri;
        }

        protected void initMaskedImageView(Bitmap loadedBitmap) {
            loadedImageBitmap = loadedBitmap;
            viewHolder.attachImageView.setOnClickListener(receiveImageFileOnClickListener());
            viewHolder.attachImageView.setImageBitmap(loadedImageBitmap);

            showAttachUI(viewHolder, isIncoming);

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

    protected static class RequestsViewHolder extends QBMessageViewHolder {
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


        public RequestsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, itemView);
        }
    }
}
