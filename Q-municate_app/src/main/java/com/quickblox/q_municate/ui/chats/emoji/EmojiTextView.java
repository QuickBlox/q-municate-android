package com.quickblox.q_municate.ui.chats.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.utils.ConstsCore;

public class EmojiTextView extends TextView {

    private int emojiconSize;

    public EmojiTextView(Context context) {
        super(context);
        init(null);
    }

    public EmojiTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) {
            emojiconSize = (int) getTextSize();
        } else {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
            emojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
            a.recycle();
        }
        setText(getText());
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (text == null) {
            super.setText(ConstsCore.EMPTY_STRING);
            return;
        }
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        EmojiCreator.addEmojis(getContext(), builder, emojiconSize);
        super.setText(builder, type);
    }

    public void setEmojiconSize(int pixels) {
        emojiconSize = pixels;
    }
}