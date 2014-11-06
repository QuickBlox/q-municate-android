package com.quickblox.q_municate.ui.chats.emoji;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.quickblox.q_municate.R;

public class EmojiEditText extends EditText {

    private int emojiconSize;

    public EmojiEditText(Context context) {
        super(context);
        emojiconSize = (int) getTextSize();
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EmojiEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        emojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        a.recycle();
        setText(getText());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        EmojiCreator.addEmojis(getContext(), getText(), emojiconSize);
    }

    public void setEmojiconSize(int pixels) {
        emojiconSize = pixels;
    }
}