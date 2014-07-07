package com.quickblox.qmunicate.ui.chats;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.ui.views.MaskGenerator;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.ImageHelper;
import com.quickblox.qmunicate.utils.ReceiveFileListener;
import com.quickblox.qmunicate.utils.ReceiveImageFileTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BaseDialogMessagesAdapter extends BaseCursorAdapter implements ReceiveFileListener {

    private final int colorMaxValue = 255;
    private final float colorAlpha = 0.8f;

    protected ScrollMessagesListener scrollMessagesListener;
    protected ImageHelper imageHelper;
    private Random random;
    private Map<Integer, Integer> colorsMap;

    public BaseDialogMessagesAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        random = new Random();
        colorsMap = new HashMap<Integer, Integer>();
        imageHelper = new ImageHelper((android.app.Activity) context);
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return Consts.MESSAGES_TYPE_COUNT;
    }

    protected boolean isOwnMessage(int senderId) {
        return senderId == currentUser.getId();
    }

    protected void displayAttachImage(String uri, final ImageView attachImageView,
            final RelativeLayout progressRelativeLayout, final RelativeLayout attachMessageRelativeLayout,
            final ProgressBar verticalProgressBar, final ProgressBar centeredProgressBar,
            boolean isOwnMessage) {
        ImageLoader.getInstance().displayImage(uri, attachImageView, Consts.UIL_DEFAULT_DISPLAY_OPTIONS,
                new SimpleImageLoading(attachImageView, progressRelativeLayout, attachMessageRelativeLayout,
                        verticalProgressBar, centeredProgressBar, isOwnMessage),
                new SimpleImageLoadingProgressListener(verticalProgressBar));
    }

    protected int getTextColor(Integer senderId) {
        if (colorsMap.get(senderId) != null) {
            return colorsMap.get(senderId);
        } else {
            int colorValue = getRandomColor();
            colorsMap.put(senderId, colorValue);
            return colorsMap.get(senderId);
        }
    }

    private int getRandomColor() {
        float[] hsv = new float[3];
        int color = Color.argb(colorMaxValue, random.nextInt(colorMaxValue), random.nextInt(colorMaxValue), random.nextInt(colorMaxValue));
        Color.colorToHSV(color, hsv);
        hsv[2] *= colorAlpha;
        color = Color.HSVToColor(hsv);
        return color;
    }

    private int getItemViewType(Cursor cursor) {
        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            return Consts.OWN_DIALOG_MESSAGE_TYPE;
        } else {
            return Consts.OPPONENT_DIALOG_MESSAGE_TYPE;
        }
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

        public SimpleImageLoading(final ImageView attachImageView,
                final RelativeLayout progressRelativeLayout, final RelativeLayout attachMessageRelativeLayout,
                final ProgressBar verticalProgressBar, final ProgressBar centeredProgressBar,
                boolean isOwnMessage) {
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
            if (isOwnMessage) {
                backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.right_bubble);
            } else {
                backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.left_bubble);
            }
            attachMessageRelativeLayout.setVisibility(View.VISIBLE);
            attachImageView.setVisibility(View.VISIBLE);
            attachImageView.setOnClickListener(receiveImageFileOnClickListener());
            this.loadedImageBitmap = loadedImageBitmap;
            hideAttachmentBackground(attachImageView);
            attachImageView.setImageBitmap(MaskGenerator.generateMask(context, backgroundBitmap,
                    loadedImageBitmap));
            scrollMessagesListener.onScrollToBottom();
        }

        private View.OnClickListener receiveImageFileOnClickListener() {
            return new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    //TODO add listener to disable logout in stopped state
                    view.startAnimation(AnimationUtils.loadAnimation(context,
                            R.anim.chat_attached_file_click));
                    new ReceiveImageFileTask(BaseDialogMessagesAdapter.this).execute(imageHelper,
                            loadedImageBitmap, false);
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