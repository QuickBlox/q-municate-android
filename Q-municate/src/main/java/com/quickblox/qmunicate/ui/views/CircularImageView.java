package com.quickblox.qmunicate.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.quickblox.qmunicate.ui.utils.ImageHelper;

public class CircularImageView extends ImageView {

    public static final String COLOR_CIRCLE_BACKGROUND = "#BAB399";
    public static final float BORDER_SIZE = 0.1f;

    public CircularImageView(Context context) {
        super(context);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        Bitmap sourceBitmap = ImageHelper.drawableToBitmap(drawable);
        Bitmap bitmap = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);

        int width = getWidth();
        int height = getHeight();

        Bitmap roundBitmap = getCroppedBitmap(bitmap, width);
        canvas.drawBitmap(roundBitmap, 0, 0, null);
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap sourceBitmap;
        if (bmp.getWidth() != radius || bmp.getHeight() != radius) {
            sourceBitmap = getCenterBitmap(bmp);
            sourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, radius, radius, false);
        } else {
            sourceBitmap = bmp;
        }
        Bitmap output = Bitmap.createBitmap(sourceBitmap.getWidth(),
                sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor(COLOR_CIRCLE_BACKGROUND));
        float circleCenterX = sourceBitmap.getWidth() / 2;
        float circleCenterY = sourceBitmap.getHeight() / 2;
        float circleRadius = sourceBitmap.getWidth() / 2 + BORDER_SIZE;
        canvas.drawCircle(circleCenterX, circleCenterY, circleRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(sourceBitmap, rect, rect, paint);

        return output;
    }

    private static Bitmap getCenterBitmap(Bitmap sourceBitmap) {
        Bitmap destinationBitmap;
        if (sourceBitmap.getWidth() >= sourceBitmap.getHeight()) {
            destinationBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    sourceBitmap.getWidth() / 2 - sourceBitmap.getHeight() / 2,
                    0,
                    sourceBitmap.getHeight(),
                    sourceBitmap.getHeight()
            );
        } else {
            destinationBitmap = Bitmap.createBitmap(
                    sourceBitmap,
                    0,
                    sourceBitmap.getHeight() / 2 - sourceBitmap.getWidth() / 2,
                    sourceBitmap.getWidth(),
                    sourceBitmap.getWidth()
            );
        }
        return destinationBitmap;
    }
}