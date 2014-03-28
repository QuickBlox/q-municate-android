package com.quickblox.qmunicate.ui.views.customFontComponents.editTexts;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.quickblox.qmunicate.ui.views.customFontComponents.FontsType;

public class LightEditText extends EditText {

    private static Typeface typeface;

    public LightEditText(Context context) {
        super(context);
    }

    public LightEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LightEditText(Context context, AttributeSet attrs, int defStyle) {
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