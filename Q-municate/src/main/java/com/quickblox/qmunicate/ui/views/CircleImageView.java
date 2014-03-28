package com.quickblox.qmunicate.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.utils.Utils;

public class CircleImageView extends ImageView {

    private final float BORDER_DP;
    private Paint mBorderPaint = new Paint();
    private Paint mMaskPaint = new Paint();

    public CircleImageView(Context context) {
        super(context);
        BORDER_DP = context.getResources().getDimension(R.dimen.image_view_avatar_border_width);
        init();
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        BORDER_DP = context.getResources().getDimension(R.dimen.image_view_avatar_border_width);
        init();
    }

    private void init() {
        setDrawingCacheEnabled(true);
        Utils.disableViewHardwareAcceleration(this);
        setScaleType(ScaleType.CENTER_CROP);

        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(getResources().getColor(R.color.avatar_border_color));
        mBorderPaint.setStrokeWidth(BORDER_DP);

        mMaskPaint.setColor(getResources().getColor(R.color.circle_view_mask_color));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap cache = getDrawingCache();
        if (cache != null) {
            drawCircle(canvas, cache);
        }
    }

    private void drawCircle(Canvas canvas, Bitmap cacheBitmap) {
        int w = this.getWidth();
        int h = this.getHeight();
        float radius = w < h ? (w / 2 - 1) : (h / 2 - 1);
        Bitmap srcBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(srcBitmap);

        c.drawCircle(w / 2, h / 2, radius - BORDER_DP, mMaskPaint); // draw mask circle
        Paint paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(srcBitmap, 0, 0, paint);
        srcBitmap.recycle();
        canvas.drawCircle(w / 2, h / 2, radius - BORDER_DP, mBorderPaint);
    }
}
