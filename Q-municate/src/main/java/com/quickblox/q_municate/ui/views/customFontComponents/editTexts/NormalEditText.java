package com.quickblox.q_municate.ui.views.customFontComponents.editTexts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.quickblox.q_municate.ui.views.customFontComponents.FontsType;

public class NormalEditText extends EditText {

    private static Typeface typeface;

    public NormalEditText(Context context) {
        super(context);
    }

    public NormalEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NormalEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), FontsType.NORMAL.getPath());
        }
        super.setTypeface(typeface);
    }
}
