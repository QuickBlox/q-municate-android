package com.quickblox.q_municate.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.InflateException;
import android.view.View;
import android.widget.ImageView;

import com.quickblox.q_municate.R;
/**
 * An {@link ImageView} that can have a mask applied to it.
 *
 * @author wijnand
 */
public class MaskedImageView extends ImageView {

    protected int mMaskResourceId;
    protected Bitmap mMask;

    protected BitmapFactory.Options mSourceOptions;

    protected Paint mSourcePaint;
    protected Paint mMaskPaint;

    /**
     * @see ImageView#ImageView(Context)
     * @param context See {@link ImageView#ImageView(Context)}.
     */
    public MaskedImageView(Context context) {
        this(context, null);
    }

    /**
     * @see ImageView#ImageView(Context, AttributeSet)
     * @param context See {@link ImageView#ImageView(Context, AttributeSet)}.
     * @param attrs See {@link ImageView#ImageView(Context, AttributeSet)}.
     */
    public MaskedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * @see ImageView#ImageView(Context, AttributeSet, int)
     * @param context See {@link ImageView#ImageView(Context, AttributeSet, int)}.
     * @param attrs See {@link ImageView#ImageView(Context, AttributeSet, int)}.
     * @param defStyle See {@link ImageView#ImageView(Context, AttributeSet, int)}.
     */
    public MaskedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Initializes this class.
     * @param attributes {@link AttributeSet} - Layout attributes.
     */
    private void init(AttributeSet attributes) {
        mSourceOptions = new BitmapFactory.Options();
        mSourceOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        if (isDeviceSdkCompatible(Build.VERSION_CODES.HONEYCOMB)) {
            mSourceOptions.inMutable = true;
        }

        mSourcePaint = new Paint();
        mSourcePaint.setAntiAlias(true);
        mSourcePaint.setFilterBitmap(true);
        mSourcePaint.setDither(true);

        mMaskPaint = new Paint();
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        if (isDeviceSdkCompatible(Build.VERSION_CODES.HONEYCOMB)) {
            // Avoid issues with hardware accelerated rendering, i.e. black background showing instead of alpha.
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        if (attributes != null) {
            parseAttributes(attributes);
        }
    }

    /**
     * @param sdkVersion int - The minimum required SDK level.
     * @return boolean - Whether or not the device SDK level meets the required one.
     */
    private boolean isDeviceSdkCompatible(int sdkVersion) {
        return Build.VERSION.SDK_INT >= sdkVersion;
    }

    /**
     * Parses the passed in {@link AttributeSet} and stores the values locally.
     * @param attributes
     */
    private void parseAttributes(AttributeSet attributes) {
        mMaskResourceId = -1;
        TypedArray array = getContext().getTheme().obtainStyledAttributes(
                attributes,
                R.styleable.MaskedImageView,
                0, 0);

        try {
            mMaskResourceId = array.getResourceId(R.styleable.MaskedImageView_mask, -1);
        } finally {
            array.recycle();
        }

        if (mMaskResourceId < 0) {
            throw new InflateException("Mandatory 'mask' attribute not set!");
        }
    }

    /**
     * Loads the image mask.
     */
    private void loadMask() {
        Drawable drawable = getResources().getDrawable(mMaskResourceId);
        mMask = drawableToBitmap(drawable);
    }

//    private Bitmap getNinepatch(Bitmap bitmap, int width, int height) {
//        byte[] chunk = bitmap.getNinePatchChunk();
//        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(getResources(), bitmap, chunk, new Rect(), null);
//        ninePatchDrawable.setBounds(Consts.ZERO_INT_VALUE, Consts.ZERO_INT_VALUE, width, height);
//        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(outputBitmap);
//        ninePatchDrawable.draw(canvas);
//        return outputBitmap;
//    }

//    public Bitmap decodeBackgroundBubbleImage(int resourcesId) {
//        return BitmapFactory.decodeResource(getResources(), resourcesId);
//    }

    /**
     * Converts a {@link Drawable} to a {@link Bitmap}.
     * @param drawable {@link Drawable} - The {@link Drawable} to convert.
     * @return {@link Bitmap} - The resulting {@link Bitmap}.
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int bitmapWidth = drawable.getIntrinsicWidth();
        int bitmapHeight = drawable.getIntrinsicHeight();
        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            // The mask may not have a width/height set, use the same as the image.
            bitmapWidth = getWidth();
            bitmapHeight = getHeight();
        }

        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawARGB(0, 0, 0, 0);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * @return {@link Rect} - The bounds to draw the source bitmap, based on the mask size.
     */
    private Rect getSourceBoundRect() {
        return new Rect(0, 0, mMask.getWidth(), mMask.getHeight());
    }

    /** {@inheritDoc} */
    @Override
    protected void onDraw(Canvas canvas) {
        Bitmap sourceBitmap = drawableToBitmap(getDrawable());

        if (sourceBitmap == null || getWidth() <= 0 || getHeight() <= 0) {
            // Bail out early when there is nothing to do.
            super.onDraw(canvas);
            return;
        }

        if (mMask == null) {
            loadMask();
        }

        Bitmap output;
        if (sourceBitmap.isMutable()) {
            output = sourceBitmap;
        } else {
            output = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        if (isDeviceSdkCompatible(Build.VERSION_CODES.HONEYCOMB_MR1)) {
            output.setHasAlpha(true);
        }

        // Make sure the original bitmap doesn't exceed the mask size.
        Rect sourceBounds = getSourceBoundRect();

        // Paint image.
        canvas.drawBitmap(output, sourceBounds, sourceBounds, mSourcePaint);

        // Set mask.
        canvas.drawBitmap(mMask, 0, 0, mMaskPaint);
    }
}