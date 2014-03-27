package com.quickblox.qmunicate.ui.views.smiles;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

import com.quickblox.qmunicate.utils.SizeUtility;

public class SmileSpan extends ImageSpan {

    private static final int SMILE_SIZE = 30;

    private Context context;
    private int sizeWidth;
    private int sizeHeight;
    private int resourceId;

    public SmileSpan(Context context, int resourceId) {
        super(context, resourceId);
        this.context = context;
        this.resourceId = resourceId;
        setBounds();
    }

    @Override
    public Drawable getDrawable() {
        Drawable drawable = context.getResources().getDrawable(resourceId);
        drawable.setBounds(0, 0, sizeWidth, sizeHeight);
        return drawable;
    }

    private void setBounds() {
        sizeWidth = SizeUtility.dipToPixels(context, SMILE_SIZE);
        sizeHeight = SizeUtility.dipToPixels(context, SMILE_SIZE);
    }
}