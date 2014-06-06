package com.quickblox.qmunicate.ui.base;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.ui.chats.ScrollMessagesListener;
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
        currentUser = App.getInstance().getUser();
        currentLoginType = App.getInstance().getUserLoginType();
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
        if(uri != null) {
            ImageLoader.getInstance().displayImage(uri, imageView, Consts.UIL_AVATAR_DISPLAY_OPTIONS);
        }
    }

    protected String getAvatarUrlForCurrentUser() {
        if (currentLoginType == LoginType.FACEBOOK) {
            return context.getString(R.string.inf_url_to_facebook_avatar, currentUser.getFacebookId());
        } else if (currentLoginType == LoginType.EMAIL) {
            return currentUser.getWebsite();
        }
        return null;
    }

    protected String getAvatarUrlForFriend(Friend friend) {
        return friend.getAvatarUrl();
    }

    public class SimpleImageLoading extends SimpleImageLoadingListener {

        private TextView pleaseWaitTextView;
        private ImageView attachImageView;
        private ProgressBar progressBar;
        private Bitmap loadedImageBitmap;

        public SimpleImageLoading(final TextView pleaseWaitTextView, final ImageView attachImageView,
                                  final ProgressBar progressBar) {
            this.pleaseWaitTextView = pleaseWaitTextView;
            this.attachImageView = attachImageView;
            this.progressBar = progressBar;
        }

        @Override
        public void onLoadingStarted(String imageUri, View view) {
            progressBar.setProgress(Consts.ZERO_INT_VALUE);
            progressBar.setVisibility(View.VISIBLE);
            pleaseWaitTextView.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            progressBar.setVisibility(View.GONE);
            pleaseWaitTextView.setVisibility(View.GONE);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, final Bitmap loadedImageBitmap) {
            progressBar.setVisibility(View.GONE);
            pleaseWaitTextView.setVisibility(View.GONE);
            attachImageView.setVisibility(View.VISIBLE);
            attachImageView.setImageBitmap(loadedImageBitmap);
            attachImageView.setOnClickListener(receiveImageFileOnClickListener());
            this.loadedImageBitmap = loadedImageBitmap;
            scrollMessagesListener.onScrollToBottom();
        }

        private View.OnClickListener receiveImageFileOnClickListener() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.chat_attached_file_click));
                    new ReceiveImageFileTask(BaseCursorAdapter.this).execute(imageHelper,
                            loadedImageBitmap, false);
                }
            };
        }
    }

    @Override
    public void onCachedImageFileReceived(File imageFile) {
    }

    @Override
    public void onAbsolutePathExtFileReceived(String absolutePath) {
        imageHelper.showFullImage(context, absolutePath);
    }
}