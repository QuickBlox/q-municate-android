package com.quickblox.qmunicate.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.quickblox.qmunicate.R;

public class CircleImageView extends ImageView {

    private int borderWidth;
    private int canvasSize;
    private Bitmap image;
    private Paint paint;
    private Paint paintBorder;

    public CircleImageView(final Context context) {
        this(context, null);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.circularImageViewStyle);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        paint = new Paint();
        paint.setAntiAlias(true);

        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CircularImageView, defStyle,
                0);

        if (attributes.getBoolean(R.styleable.CircularImageView_border, true)) {
            setBorderWidth(attributes.getColor(R.styleable.CircularImageView_border_width, 4));
            setBorderColor(attributes.getInt(R.styleable.CircularImageView_border_color, Color.WHITE));
        }

        if (attributes.getBoolean(R.styleable.CircularImageView_shadow, false)) {
            addShadow();
        }
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = borderWidth;
        this.requestLayout();
        this.invalidate();
    }

    public void setBorderColor(int borderColor) {
        if (paintBorder != null) {
            paintBorder.setColor(borderColor);
        }
        this.invalidate();
    }

    public void addShadow() {
        setLayerType(LAYER_TYPE_SOFTWARE, paintBorder);
        paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    @Override
    public void onDraw(Canvas canvas) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) this.getDrawable();
        if (bitmapDrawable != null) {
            image = bitmapDrawable.getBitmap();
        }

        if (image != null) {

            canvasSize = canvas.getWidth();
            if (canvas.getHeight() < canvasSize) {
                canvasSize = canvas.getHeight();
            }

            BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(image, canvasSize, canvasSize,
                    false), Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            paint.setShader(shader);

            int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth,
                    ((canvasSize - (borderWidth * 2)) / 2) + borderWidth - 4.0f, paintBorder);
            canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth,
                    ((canvasSize - (borderWidth * 2)) / 2) - 4.0f, paint);
        }
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }

        return result;
    }

    private int measureHeight(int measureSpecHeight) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpecHeight);
        int specSize = MeasureSpec.getSize(measureSpecHeight);

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else {
            result = canvasSize;
        }

        return (result + 2);
    }
}