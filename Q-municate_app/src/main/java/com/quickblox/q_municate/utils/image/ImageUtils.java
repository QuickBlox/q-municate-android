package com.quickblox.q_municate.utils.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.TypedValue;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.MimeType;
import com.quickblox.q_municate.utils.StorageUtil;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageUtils {

    public static final int GALLERY_REQUEST_CODE = 111;
    public static final int CAMERA_REQUEST_CODE = 222;
    public static final int IMAGE_REQUEST_CODE = 333;

    private static final String TAG = ImageUtils.class.getSimpleName();
    private static final String CAMERA_FILE_NAME_PREFIX = "CAMERA_";
    private static final String CAMERA_FILE_EXT = ".jpg";
    private static final String CAMERA_FILE_NAME = CAMERA_FILE_NAME_PREFIX + DateUtilsCore.getCurrentTime() + CAMERA_FILE_EXT;
    private static final int AVATAR_SIZE = 110;

    private Activity activity;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    public static void startImagePicker(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MimeType.IMAGE_MIME);
        activity.startActivityForResult(
                Intent.createChooser(intent, activity.getString(R.string.dlg_choose_image_from)),
                GALLERY_REQUEST_CODE);
    }

    public static void startImagePicker(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(MimeType.IMAGE_MIME);
        fragment.startActivityForResult(
                Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_image_from)),
                GALLERY_REQUEST_CODE);
    }

    public static void startCameraForResult(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            return;
        }

        File photoFile = getTemporaryCameraFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        activity.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static void startCameraForResult(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) == null) {
            return;
        }

        File photoFile = getTemporaryCameraFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
        fragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    public static File getTemporaryCameraFile() {
        File storageDir = StorageUtil.getAppExternalDataDirectoryFile();
        File file = new File(storageDir, CAMERA_FILE_NAME);
        try {
            file.createNewFile();
        } catch (IOException e) {
            ErrorUtils.logError(e);
        }
        return file;
    }

    public static File getLastUsedCameraFile() {
        File dataDir = StorageUtil.getAppExternalDataDirectoryFile();
        File[] files = dataDir.listFiles();
        List<File> filteredFiles = new ArrayList<>();
        for (File file : files) {
            if (file.getName().startsWith(CAMERA_FILE_NAME_PREFIX)) {
                filteredFiles.add(file);
            }
        }

        Collections.sort(filteredFiles);
        if (!filteredFiles.isEmpty()) {
            return filteredFiles.get(filteredFiles.size() - 1);
        } else {
            return null;
        }
    }

    public static String saveUriToFile(Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor = App.getInstance().getContentResolver()
                .openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        InputStream inputStream = new FileInputStream(fileDescriptor);
        BufferedInputStream bis = new BufferedInputStream(inputStream);

        File parentDir = StorageUtil.getAppExternalDataDirectoryFile();
        String fileName = String.valueOf(System.currentTimeMillis()) + CAMERA_FILE_EXT;
        File resultFile = new File(parentDir, fileName);

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(resultFile));

        byte[] buf = new byte[2048];
        int length;

        try {
            while ((length = bis.read(buf)) > 0) {
                bos.write(buf, 0, length);
            }
        } catch (Exception e) {
            throw new IOException("Can\'t save Storage API bitmap to a file!", e);
        } finally {
            parcelFileDescriptor.close();
            bis.close();
            bos.close();
        }

        return resultFile.getAbsolutePath();
    }

    public static File getCreatedFileFromUri(Uri uri) {
        String filePath;
        File file = null;
        try {
            if (uri != null) {
                filePath = ImageUtils.saveUriToFile(uri);
                file = new File(filePath);
            }
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
        return file;
    }

    private static int getExifInterfaceOrientation(String pathToFile) {
        int orientation = ConstsCore.NOT_INITIALIZED_VALUE;

        try {
            ExifInterface exifInterface = new ExifInterface(pathToFile);
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ConstsCore.NOT_INITIALIZED_VALUE);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }

        return orientation;
    }

    public static void checkForRotation(String imagePath) {

        //    public static void checkForRotation(String imagePath) throws IOException {
        //        ExifInterface exifInterface = new ExifInterface(imagePath);
        //        Log.d("test_rotation", "EXIF value " + exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION) + ", imagePath = " + imagePath);
        //        if (exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
        //            Log.d("test_rotation", "EXIF value " + 90);
        //            rotateImage(imagePath, 90);
        //        } else if (exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
        //            Log.d("test_rotation", "EXIF value " + 270);
        //            rotateImage(imagePath, 270);
        //        } else if (exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
        //            Log.d("test_rotation", "EXIF value " + 180);
        //            rotateImage(imagePath, 180);
        //        }
        //}

        int orientation = getExifInterfaceOrientation(imagePath);

        if (orientation == 0) {
            return;
        }

        try {
            Bitmap bitmap = getBitmapFromFile(imagePath);

            Matrix matrix = new Matrix();

            switch (orientation) {
                case 2:
                    matrix.setScale(-1, 1);
                    break;
                case 3:
                    matrix.setRotate(180);
                    break;
                case 4:
                    matrix.setRotate(180);
                    matrix.postScale(-1, 1);
                    break;
                case 5:
                    matrix.setRotate(90);
                    matrix.postScale(-1, 1);
                    break;
                case 6:
                    matrix.setRotate(90);
                    break;
                case 7:
                    matrix.setRotate(-90);
                    matrix.postScale(-1, 1);
                    break;
                case 8:
                    matrix.setRotate(-90);
                    break;
                default:
                    return;
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            saveFileFromBitmap(bitmap, imagePath);
        } catch (Exception e) {
            ErrorUtils.logError(e);
        }
    }

    public static void saveFileFromBitmap(Bitmap bitmap, String fileName) {
        File file = new File(fileName);
        ByteArrayOutputStream byteArrayOutputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, ConstsCore.FULL_QUALITY, byteArrayOutputStream);
            byte[] bitmapData = byteArrayOutputStream.toByteArray();
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bitmapData);
            Utils.closeOutputStream(fileOutputStream);
            Utils.closeOutputStream(byteArrayOutputStream);
        } catch (IOException e) {
            ErrorUtils.logError(TAG, e);
        } finally {
            Utils.closeOutputStream(fileOutputStream);
            Utils.closeOutputStream(byteArrayOutputStream);
        }
    }

    public static Bitmap getBitmapFromFile(String filePath) {
        return BitmapFactory.decodeFile(filePath, getBitmapOption());
    }

    private static BitmapFactory.Options getBitmapOption() {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bitmapOptions.inDither = false;
        bitmapOptions.inPurgeable = true;
        bitmapOptions.inInputShareable = true;
        bitmapOptions.inTempStorage = new byte[32 * 1024];
        return bitmapOptions;
    }

    private static Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight,
            ScalingLogic scalingLogic) {
        Rect srcRect = calculateSrcRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth,
                dstHeight, scalingLogic);
        Rect dstRect = calculateDstRect(unscaledBitmap.getWidth(), unscaledBitmap.getHeight(), dstWidth,
                dstHeight, scalingLogic);
        Bitmap scaledBitmap = Bitmap.createBitmap(dstRect.width(), dstRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(scaledBitmap);
        canvas.drawBitmap(unscaledBitmap, srcRect, dstRect, new Paint(Paint.FILTER_BITMAP_FLAG));
        return scaledBitmap;
    }

    private static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
            ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                final int srcRectWidth = (int) (srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, ConstsCore.ZERO_INT_VALUE, srcRectLeft + srcRectWidth,
                        srcHeight);
            } else {
                final int srcRectHeight = (int) (srcWidth / dstAspect);
                final int scrRectTop = (int) (srcHeight - srcRectHeight) / 2;
                return new Rect(ConstsCore.ZERO_INT_VALUE, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        } else {
            return new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, srcWidth, srcHeight);
        }
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
            ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float) srcWidth / (float) srcHeight;
            final float dstAspect = (float) dstWidth / (float) dstHeight;

            if (srcAspect > dstAspect) {
                return new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, dstWidth,
                        (int) (dstWidth / srcAspect));
            } else {
                return new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE,
                        (int) (dstHeight * srcAspect), dstHeight);
            }
        } else {
            return new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, dstWidth, dstHeight);
        }
    }

    public static Drawable getRoundIconDrawable(Context context, Bitmap avatarBitmap) {
        int actionBarHeight = getActionBarHeight(context);
        return getRoundIconDrawable(avatarBitmap, actionBarHeight);
    }

    private static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        int actionBarHeight = AVATAR_SIZE;

        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue
                    .complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        }

        int margin = actionBarHeight / 10;

        return actionBarHeight - margin;
    }

    private static Drawable getRoundIconDrawable(Bitmap avatarBitmap, int size) {
        // TODO Remove freaking hardcoded values
        Resources res = App.getInstance().getResources();
        Bitmap scaledBitmap = createScaledBitmap(avatarBitmap, size, size, ScalingLogic.CROP);

        // create rounded image avatar
        Bitmap output = Bitmap
                .createBitmap(scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        int color = App.getInstance().getResources().getColor(R.color.gray);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        RectF rectF = new RectF(rect);
        float roundPx = 200;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(scaledBitmap, rect, rect, paint);

        // draw two bitmaps on Canvas
        int width = output.getWidth();
        int height = output.getHeight();

        Bitmap finalImage = Bitmap.createBitmap(width, height, output.getConfig());
        canvas = new Canvas(finalImage);
        canvas.drawBitmap(output, 0, 0, null);

        return new BitmapDrawable(res, finalImage);
    }

    private enum ScalingLogic {
        CROP, FIT
    }
}