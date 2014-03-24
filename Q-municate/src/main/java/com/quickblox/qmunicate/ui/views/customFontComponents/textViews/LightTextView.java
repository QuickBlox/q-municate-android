package com.quickblox.qmunicate.ui.views.customFontComponents.textViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.quickblox.qmunicate.ui.views.customFontComponents.FontsType;

public class LightTextView extends TextView {

    private static Typeface typeface;

    public LightTextView(Context context) {
        super(context);
    }

    public LightTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), FontsType.LIGHT.getPath());
        }
        super.setTypeface(typeface);
    }
}
