package com.quickblox.qmunicate.ui.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.ui.chats.ScrollMessagesListener;
import com.quickblox.qmunicate.ui.views.MaskGenerator;
import com.quickblox.qmunicate.utils.AppSessionHelper;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ImageHelper;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;

import java.io.File;

public abstract class BaseCursorAdapter extends CursorAdapter implements ReceiveFileListener {

    protected final Context context;
    protected final Resources resources;
    protected final LayoutInflater layoutInflater;

    protected QBUser currentUser;
    protected LoginType currentLoginType;
    protected ImageHelper imageHelper;
    protected ScrollMessagesListener scrollMessagesListener;

    public BaseCursorAdapter(Context context, Cursor cursor, boolean autoRequery) {
        super(context, cursor, autoRequery);
        this.context = context;
        resources = context.getResources();
        layoutInflater = LayoutInflater.from(context);
        currentUser = AppSessionHelper.getSession().getUser();
        currentLoginType = AppSessionHelper.getSession().getLoginType();
        imageHelper = new ImageHelper((android.app.Activity) context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }

    protected void displayAvatarImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
    }

    protected String getAvatarUrlForCurrentUser() {
        return currentUser.getWebsite();
    }

    protected String getAvatarUrlForFriend(Friend friend) {
        return friend.getAvatarUrl();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void hideAttachmentBackground(ImageView attachImageView) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            attachImageView.setBackgroundDrawable(null);
        } else {
            attachImageView.setBackground(null);
        }
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
        imageHelper.showFullImage(context, absolutePath);
    }

    public class SimpleImageLoading extends SimpleImageLoadingListener {

        private RelativeLayout progressRelativeLayout;
        private RelativeLayout attachMessageRelativeLayout;
        private ImageView attachImageView;
        private ProgressBar verticalProgressBar;
        private ProgressBar centeredProgressBar;
        private Bitmap loadedImageBitmap;
        private boolean isOwnMessage;

        public SimpleImageLoading(final ImageView attachImageView, final RelativeLayout progressRelativeLayout,
                                  final RelativeLayout attachMessageRelativeLayout, final ProgressBar verticalProgressBar,
                                  final ProgressBar centeredProgressBar, boolean isOwnMessage) {
            this.progressRelativeLayout = progressRelativeLayout;
            this.attachMessageRelativeLayout = attachMessageRelativeLayout;
            this.attachImageView = attachImageView;
            this.verticalProgressBar = verticalProgressBar;
            this.centeredProgressBar = centeredProgressBar;
            this.isOwnMessage = isOwnMessage;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            verticalProgressBar.setProgress(Consts.ZERO_INT_VALUE);
            verticalProgressBar.setVisibility(View.VISIBLE);
            centeredProgressBar.setProgress(Consts.ZERO_INT_VALUE);
            centeredProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            verticalProgressBar.setVisibility(View.GONE);
            centeredProgressBar.setVisibility(View.GONE);
            progressRelativeLayout.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImageBitmap) {
            Bitmap backgroundBitmap;
            verticalProgressBar.setVisibility(View.GONE);
            centeredProgressBar.setVisibility(View.GONE);
            if (progressRelativeLayout != null) {
                progressRelativeLayout.setVisibility(View.GONE);
            }
            if(isOwnMessage) {
                backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.right_bubble);
            } else {
                backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.left_bubble);
            }
            attachMessageRelativeLayout.setVisibility(View.VISIBLE);
            attachImageView.setVisibility(View.VISIBLE);
            attachImageView.setOnClickListener(receiveImageFileOnClickListener());
            this.loadedImageBitmap = loadedImageBitmap;
            scrollMessagesListener.onScrollToBottom();
            hideAttachmentBackground(attachImageView);
            attachImageView.setImageBitmap(MaskGenerator.generateMask(context, backgroundBitmap, loadedImageBitmap));
        }

        private View.OnClickListener receiveImageFileOnClickListener() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    //TODO add listener to disable logout in stopped state
                    view.startAnimation(AnimationUtils.loadAnimation(context,
                            R.anim.chat_attached_file_click));
                    new ReceiveImageFileTask(BaseCursorAdapter.this).execute(imageHelper, loadedImageBitmap,
                            false);
                }
            };
        }
    }

    public class SimpleImageLoadingProgressListener implements ImageLoadingProgressListener {

        private ProgressBar verticalProgressBar;

        public SimpleImageLoadingProgressListener(ProgressBar verticalProgressBar) {
            this.verticalProgressBar = verticalProgressBar;
        }

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            verticalProgressBar.setProgress(Math.round(100.0f * current / total));
        }
    }
}