package com.quickblox.q_municate.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.InflateException;
import android.widget.ImageView;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.utils.ConstsCore;

public class MaskedImageView extends ImageView {

    private Paint maskedPaint;
    private Paint copyPaint;
    private Drawable maskDrawable;
    private int maskResourceId;
    private Rect boundsRect;
    private RectF boundsRectF;

    public MaskedImageView(Context context) {
        this(context, null);
    }

    public MaskedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        maskResourceId = -1;
        TypedArray array = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.MaskedImageView,
                ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE);

        try {
            maskResourceId = array.getResourceId(R.styleable.MaskedImageView_mask, -1);
        } finally {
            array.recycle();
        }

        if (maskResourceId < ConstsCore.ZERO_INT_VALUE) {
            throw new InflateException("Mandatory 'mask' attribute not set!");
        }

        maskedPaint = new Paint();
        maskedPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));

        copyPaint = new Paint();
        maskDrawable = getResources().getDrawable(maskResourceId);
    }

    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        boundsRect = new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, width, height);
        boundsRectF = new RectF(boundsRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int sc = canvas.saveLayer(boundsRectF, copyPaint,
                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG | Canvas.FULL_COLOR_LAYER_SAVE_FLAG);

        maskDrawable.setBounds(boundsRect);
        maskDrawable.draw(canvas);

        canvas.saveLayer(boundsRectF, maskedPaint, ConstsCore.ZERO_INT_VALUE);

        super.onDraw(canvas);

        canvas.restoreToCount(sc);
    }
}