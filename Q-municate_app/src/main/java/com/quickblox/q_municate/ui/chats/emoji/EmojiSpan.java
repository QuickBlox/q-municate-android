package com.quickblox.q_municate.ui.chats.emoji;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.DynamicDrawableSpan;

import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;

class EmojiSpan extends DynamicDrawableSpan {

    private final Context context;
    private final int resourceId;
    private final int size;
    private Drawable drawable;

    public EmojiSpan(Context context, int resourceId, int size) {
        super();
        this.context = context;
        this.resourceId = resourceId;
        this.size = size;
    }

    public Drawable getDrawable() {
        if (drawable == null) {
            try {
                drawable = context.getResources().getDrawable(resourceId);
                int size = this.size;
                drawable.setBounds(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, size, size);
            } catch (Exception e) {
                ErrorUtils.logError(e);
            }
        }
        return drawable;
    }
}