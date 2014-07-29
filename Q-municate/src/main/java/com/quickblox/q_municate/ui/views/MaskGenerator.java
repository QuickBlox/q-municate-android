package com.quickblox.q_municate.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;

import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.ImageHelper;
import com.quickblox.q_municate.utils.SizeUtility;

public class MaskGenerator {

    public static Bitmap generateMask(Context context, Bitmap mask, Bitmap original) {
        int width = SizeUtility.dipToPixels(context, Consts.CHAT_ATTACH_WIDTH);
        original = ImageHelper.getScaledBitmap(original, original.getWidth(), original.getHeight(), width);
        Bitmap result = Bitmap.createBitmap(width, original.getHeight(), Bitmap.Config.ARGB_8888);
        mask = getNinepatch(context.getResources(), mask, width, original.getHeight());
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        canvas.drawBitmap(original, Consts.ZERO_INT_VALUE, Consts.ZERO_INT_VALUE, null);
        canvas.drawBitmap(mask, Consts.ZERO_INT_VALUE, Consts.ZERO_INT_VALUE, paint);
        paint.setXfermode(null);
        return result;
    }

    public static Bitmap getNinepatch(Resources resource, Bitmap bitmap, int x, int y) {
        byte[] chunk = bitmap.getNinePatchChunk();
        NinePatchDrawable ninePatchDrawable = new NinePatchDrawable(resource, bitmap, chunk, new Rect(), null);
        ninePatchDrawable.setBounds(Consts.ZERO_INT_VALUE, Consts.ZERO_INT_VALUE, x, y);
        Bitmap outputBitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outputBitmap);
        ninePatchDrawable.draw(canvas);
        return outputBitmap;
    }
}