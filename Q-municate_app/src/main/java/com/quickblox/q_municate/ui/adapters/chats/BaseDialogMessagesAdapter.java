package com.quickblox.q_municate.ui.adapters.chats;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.auth.QBAuth;
import com.quickblox.core.exception.BaseServiceException;
import com.quickblox.core.server.BaseService;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.others.PreviewImageActivity;
import com.quickblox.q_municate.ui.adapters.base.BaseClickListenerViewHolder;
import com.quickblox.q_municate.ui.adapters.base.BaseRecyclerViewAdapter;
import com.quickblox.q_municate.ui.adapters.base.BaseViewHolder;
import com.quickblox.q_municate.ui.views.maskedimageview.MaskedImageView;
import com.quickblox.q_municate.ui.views.roundedimageview.RoundedImageView;
import com.quickblox.q_municate.utils.ColorUtils;
import com.quickblox.q_municate.utils.DateUtils;
import com.quickblox.q_municate.utils.FileUtils;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate.utils.listeners.ChatUIHelperListener;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.CombinationMessage;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Dialog;
import com.quickblox.users.model.QBUser;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

import butterknife.Bind;

public abstract class BaseDialogMessagesAdapter
        extends BaseRecyclerViewAdapter<CombinationMessage, BaseClickListenerViewHolder<CombinationMessage>> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    protected static final int TYPE_REQUEST_MESSAGE = 0;
    protected static final int TYPE_OWN_MESSAGE = 1;
    protected static final int TYPE_OPPONENT_MESSAGE = 2;

    protected DataManager dataManager;
    protected ChatUIHelperListener chatUIHelperListener;
    protected ImageUtils imageUtils;
    protected Dialog dialog;
    protected ColorUtils colorUtils;
    protected QBUser currentUser;

    private FileUtils fileUtils;

    public BaseDialogMessagesAdapter(BaseActivity baseActivity, List<CombinationMessage> objectsList) {
        super(baseActivity, objectsList);
        dataManager = DataManager.getInstance();
        imageUtils = new ImageUtils(baseActivity);
        colorUtils = new ColorUtils();
        fileUtils = new FileUtils();
        currentUser = AppSession.getSession().getUser();
    }

    @Override
    public long getHeaderId(int position) {
        CombinationMessage combinationMessage = getItem(position);
        return DateUtils.toShortDateLong(combinationMessage.getCreatedDate());
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = layoutInflater.inflate(R.layout.item_chat_sticky_header_date, parent, false);
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

    protected void displayAttachImageById(String attachId, final ViewHolder viewHolder) {
        String token;
        String privateUrl;
        try {
            token = QBAuth.getBaseService().getToken();
            privateUrl = String.format("%s/blobs/%s?token=%s", BaseService.getServiceEndpointURL(), attachId, token);
            displayAttachImage(privateUrl, viewHolder);
        } catch (BaseServiceException e) {
            e.printStackTrace();
        }
//        TODO VT надо переделать на след код после обновления версии SDK до 2.5
//        String privateUrl = QBFile.getPrivateUrlForUID(attachId);
//        displayAttachImage(privateUrl, viewHolder);
    }

    protected void displayAttachImage(String attachUrl, final ViewHolder viewHolder) {
        ImageLoader.getInstance().displayImage(attachUrl, viewHolder.attachImageView,
                ImageLoaderUtils.UIL_DEFAULT_DISPLAY_OPTIONS, new ImageLoadingListener(viewHolder),
                new SimpleImageLoadingProgressListener(viewHolder));
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

    protected void setMessageStatus(ImageView imageView, boolean messageDelivered, boolean messageRead) {
        imageView.setImageResource(getMessageStatusIconId(messageDelivered, messageRead));
    }

    protected int getItemViewType(CombinationMessage combinationMessage) {
        boolean ownMessage = !combinationMessage.isIncoming(currentUser.getId());
        if (combinationMessage.getNotificationType() == null) {
            if (ownMessage) {
                return TYPE_OWN_MESSAGE;
            } else {
                return TYPE_OPPONENT_MESSAGE;
            }
        } else {
            return TYPE_REQUEST_MESSAGE;
        }
    }

    protected void resetUI(ViewHolder viewHolder) {
        setViewVisibility(viewHolder.attachMessageRelativeLayout, View.GONE);
        setViewVisibility(viewHolder.progressRelativeLayout, View.GONE);
        setViewVisibility(viewHolder.textMessageView, View.GONE);
    }

    //    @Override
    //    public void onAbsolutePathExtFileReceived(String absolutePath) {
    //        chatUIHelperListener.onScreenResetPossibilityPerformLogout(false);
    //        imageUtils.showFullImage((android.app.Activity) context, absolutePath);
    //    }

    protected void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    protected static class ViewHolder extends BaseViewHolder<CombinationMessage> {

        @Nullable
        @Bind(R.id.avatar_imageview)
        RoundedImageView avatarImageView;

        @Nullable
        @Bind(R.id.name_textview)
        TextView nameTextView;

        @Nullable
        @Bind(R.id.text_message_view)
        View textMessageView;

        @Nullable
        @Bind(R.id.text_message_delivery_status_imageview)
        ImageView messageDeliveryStatusImageView;

        @Nullable
        @Bind(R.id.attach_message_delivery_status_imageview)
        ImageView attachDeliveryStatusImageView;

        @Nullable
        @Bind(R.id.progress_relativelayout)
        RelativeLayout progressRelativeLayout;

        @Nullable
        @Bind(R.id.attach_message_relativelayout)
        RelativeLayout attachMessageRelativeLayout;

        @Nullable
        @Bind(R.id.message_textview)
        TextView messageTextView;

        @Nullable
        @Bind(R.id.attach_imageview)
        MaskedImageView attachImageView;

        @Nullable
        @Bind(R.id.time_text_message_textview)
        TextView timeTextMessageTextView;

        @Nullable
        @Bind(R.id.time_attach_message_textview)
        TextView timeAttachMessageTextView;

        @Nullable
        @Bind(R.id.vertical_progressbar)
        ProgressBar verticalProgressBar;

        @Nullable
        @Bind(R.id.centered_progressbar)
        ProgressBar centeredProgressBar;

        @Nullable
        @Bind(R.id.accept_friend_imagebutton)
        ImageView acceptFriendImageView;

        @Nullable
        @Bind(R.id.divider_view)
        View dividerView;

        @Nullable
        @Bind(R.id.reject_friend_imagebutton)
        ImageView rejectFriendImageView;

        public ViewHolder(BaseDialogMessagesAdapter adapter, View view) {
            super(adapter, view);
        }
    }

    public class ImageLoadingListener extends SimpleImageLoadingListener {

        private ViewHolder viewHolder;
        private Bitmap loadedImageBitmap;
        private String imageUrl;

        public ImageLoadingListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            super.onLoadingStarted(imageUri, view);
            viewHolder.verticalProgressBar.setProgress(ConstsCore.ZERO_INT_VALUE);
            viewHolder.centeredProgressBar.setProgress(ConstsCore.ZERO_INT_VALUE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            updateUIAfterLoading();
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
            setViewVisibility(viewHolder.attachMessageRelativeLayout, View.VISIBLE);
            setViewVisibility(viewHolder.attachImageView, View.VISIBLE);

            updateUIAfterLoading();
        }

        private void updateUIAfterLoading() {
            if (viewHolder.progressRelativeLayout != null) {
                setViewVisibility(viewHolder.progressRelativeLayout, View.GONE);
            }
        }

        private View.OnClickListener receiveImageFileOnClickListener() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (imageUrl != null) {
                        view.startAnimation(AnimationUtils.loadAnimation(baseActivity, R.anim.chat_attached_file_click));
                        PreviewImageActivity.start(baseActivity, imageUrl);
                    }
                }
            };
        }
    }

    public class SimpleImageLoadingProgressListener implements ImageLoadingProgressListener {

        private ViewHolder viewHolder;

        public SimpleImageLoadingProgressListener(ViewHolder viewHolder) {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            viewHolder.verticalProgressBar.setProgress(Math.round(100.0f * current / total));
        }
    }
}