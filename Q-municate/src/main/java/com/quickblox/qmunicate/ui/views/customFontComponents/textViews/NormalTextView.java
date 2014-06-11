package com.quickblox.qmunicate.ui.views.customFontComponents.textViews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.quickblox.qmunicate.ui.views.customFontComponents.FontsType;

public class NormalTextView extends TextView {

    private static Typeface typeface;

    public NormalTextView(Context context) {
        super(context);
    }

    public NormalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NormalTextView(Context context, AttributeSet attrs, int defStyle) {
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
