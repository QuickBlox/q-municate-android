package com.quickblox.qmunicate.ui.views.customFontComponents.textViews;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.widget.TextView;

import com.quickblox.qmunicate.ui.views.customFontComponents.FontsType;

public class AutofitBoldTextView extends TextView {

    private static final String TAG = AutofitBoldTextView.class.getSimpleName();
    private static final boolean SPEW = false;
    private static final int DEFAULT_MIN_TEXT_SIZE = 8;
    private static final float PRECISION = 0.5f;
    private static Typeface typeface;

    private float mMinTextSize;
    private float mMaxTextSize;
    private float mPrecision;
    private Paint mPaint;

    public AutofitBoldTextView(Context context) {
        super(context);
        init();
    }

    public AutofitBoldTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public float getMaxTextSize() {
        return mMaxTextSize;
    }

    public void setMaxTextSize(int maxTextSize) {
        mMaxTextSize = maxTextSize;
    }

    public float getMinTextSize() {
        return mMinTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        mMinTextSize = minTextSize;
    }

    public float getPrecision() {
        return mPrecision;
    }

    public void setPrecision(float precision) {
        mPrecision = precision;
    }

    public void refitText(String text, int width) {
        if (width > 0) {
            Context context = getContext();
            Resources r = Resources.getSystem();

            int targetWidth = width - getPaddingLeft() - getPaddingRight();
            float newTextSize = mMaxTextSize;
            float high = mMaxTextSize;
            float low = 0;

            if (context != null) {
                r = context.getResources();
            }

            mPaint.set(getPaint());
            mPaint.setTextSize(newTextSize);

            if (mPaint.measureText(text) > targetWidth) {
                newTextSize = getTextSize(r, text, targetWidth, low, high);

                if (newTextSize < mMinTextSize) {
                    newTextSize = mMinTextSize;
                }
            }

            setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);

        }
    }

    @Override
    public void setTypeface(Typeface tf, int style) {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), FontsType.BOLD.getPath());
        }
        super.setTypeface(typeface);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        refitText(getText().toString(), parentWidth);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) {
            refitText(getText().toString(), w);
        }
    }

    private void init() {
        mMinTextSize = DEFAULT_MIN_TEXT_SIZE;
        mMaxTextSize = getTextSize();
        mPrecision = PRECISION;
        mPaint = new Paint();
    }

    // Recursive binary search to find the best size for the text
    private float getTextSize(Resources resources, String text, float targetWidth, float low, float high) {
        float mid = (low + high) / 2.0f;

        mPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, mid, resources.getDisplayMetrics()));
        float textWidth = mPaint.measureText(text);

        if (SPEW)
            Log.d(TAG, "low=" + low + " high=" + high + " mid=" + mid + " target=" + targetWidth + " width=" + textWidth);

        if ((high - low) < mPrecision) {
            return low;
        } else if (textWidth > targetWidth) {
            return getTextSize(resources, text, targetWidth, low, mid);
        } else if (textWidth < targetWidth) {
            return getTextSize(resources, text, targetWidth, mid, high);
        } else {
            return mid;
        }
    }
}