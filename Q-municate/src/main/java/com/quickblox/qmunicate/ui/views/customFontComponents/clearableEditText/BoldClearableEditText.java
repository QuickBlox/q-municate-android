package com.quickblox.qmunicate.ui.views.customFontComponents.clearableEditText;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.views.customFontComponents.editTexts.BoldEditText;

public class BoldClearableEditText extends BoldEditText {

    private static final int RIGHT_DRAWBLE = 2;
    private Drawable clearButtonImage;

    public BoldClearableEditText(final Context context) {
        super(context);
        init();
    }

    public BoldClearableEditText(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BoldClearableEditText(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public String getTextFromField() {
        return getText().toString();
    }

    private void init() {
        clearButtonImage = getResources().getDrawable(R.drawable.ic_action_delete);
        setFocusableInTouchMode(true);
        addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                Drawable[] drawables = getCompoundDrawables();
                if (editable.toString().isEmpty()) {
                    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], null, drawables[3]);
                } else if (!isRightDrawableSet() && hasFocus()) {
                    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], clearButtonImage, drawables[3]);
                }
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View v, final MotionEvent event) {
                if (!isRightDrawableSet()) {
                    return false;
                }

                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }

                if (event.getX() > (getWidth() - getPaddingRight() - clearButtonImage.getIntrinsicWidth())) {
                    setText("");
                }
                return false;
            }
        });

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Drawable[] drawables = getCompoundDrawables();
                if (hasFocus && !TextUtils.isEmpty(getText())) {
                    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], clearButtonImage, drawables[3]);
                } else {
                    setCompoundDrawablesWithIntrinsicBounds(drawables[0], drawables[1], null, drawables[3]);
                }
            }
        });
    }

    private boolean isRightDrawableSet() {
        return getCompoundDrawables()[RIGHT_DRAWBLE] != null;
    }
}
