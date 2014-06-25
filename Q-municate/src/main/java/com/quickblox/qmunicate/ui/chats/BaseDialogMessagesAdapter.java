package com.quickblox.qmunicate.ui.chats;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.qmunicate.caching.tables.DialogMessageTable;
import com.quickblox.qmunicate.ui.base.BaseCursorAdapter;
import com.quickblox.qmunicate.utils.Consts;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BaseDialogMessagesAdapter extends BaseCursorAdapter {

    private final int colorMaxValue = 255;
    private Random random;
    private Map<Integer, Integer> colorsMap;

    public BaseDialogMessagesAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        random = new Random();
        colorsMap = new HashMap<Integer, Integer>();
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

    protected void displayAttachImage(String uri, final ImageView attachImageView, final RelativeLayout progressRelativeLayout,
                                      final RelativeLayout attachMessageRelativeLayout, final ProgressBar verticalProgressBar,
                                      final ProgressBar centeredProgressBar, boolean isOwnMessage) {
        ImageLoader.getInstance().displayImage(uri, attachImageView, Consts.UIL_DEFAULT_DISPLAY_OPTIONS,
                new SimpleImageLoading(attachImageView, progressRelativeLayout, attachMessageRelativeLayout, verticalProgressBar, centeredProgressBar, isOwnMessage),
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
        return Color.argb(colorMaxValue, random.nextInt(colorMaxValue), random.nextInt(colorMaxValue), random.nextInt(colorMaxValue));
    }

    private int getItemViewType(Cursor cursor) {
        int senderId = cursor.getInt(cursor.getColumnIndex(DialogMessageTable.Cols.SENDER_ID));
        if (isOwnMessage(senderId)) {
            return Consts.OWN_DIALOG_MESSAGE_TYPE;
        } else {
            return Consts.OPPONENT_DIALOG_MESSAGE_TYPE;
        }
    }
}