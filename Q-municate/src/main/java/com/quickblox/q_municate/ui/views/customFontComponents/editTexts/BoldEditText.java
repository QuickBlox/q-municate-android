package com.quickblox.q_municate.ui.views.customFontComponents.editTexts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.quickblox.q_municate.ui.views.customFontComponents.FontsType;

public class BoldEditText extends EditText {

    private static Typeface typeface;

    public BoldEditText(Context context) {
        super(context);
    }

    public BoldEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BoldEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), FontsType.BOLD.getPath());
        }
        super.setTypeface(typeface);
    }
}