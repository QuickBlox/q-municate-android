package com.quickblox.q_municate.utils;

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
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.location.MapsActivity;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.DateUtilsCore;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MediaUtils {

    public static final int GALLERY_REQUEST_CODE = 111;
    public static final int CAMERA_PHOTO_REQUEST_CODE = 222;
    public static final int CAMERA_VIDEO_REQUEST_CODE = 232;
    public static final int IMAGE_REQUEST_CODE = 333;
    public static final int IMAGE_VIDEO_LOCATION_REQUEST_CODE = 444;

    private static final String TAG = MediaUtils.class.getSimpleName();
    private static final String CAMERA_FILE_NAME_PREFIX = "CAMERA_";
    private static final String CAMERA_PHOTO_FILE_EXT = ".jpg";
    private static final String CAMERA_VIDEO_FILE_EXT = ".mp4";
    private static final int AVATAR_SIZE = 110;

    private Activity activity;

    public MediaUtils(Activity activity) {
        this.activity = activity;
    }

    public static void startMediaPicker(Activity activity) {
        Intent intent = new Intent();
        setIntentMediaPicker(intent);
        activity.startActivityForResult(
                Intent.createChooser(intent, activity.getString(R.string.dlg_choose_media_from)),
                GALLERY_REQUEST_CODE);
    }

    public static void startMediaPicker(Fragment fragment) {
        Intent intent = new Intent();
        setIntentMediaPicker(intent);
        fragment.startActivityForResult(
                Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_media_from)),
                GALLERY_REQUEST_CODE);
    }

    public static void startImagePicker(Activity activity) {
        Intent intent = new Intent();
        setIntentImagePicker(intent);
        activity.startActivityForResult(
                Intent.createChooser(intent, activity.getString(R.string.dlg_choose_image_from)),
                GALLERY_REQUEST_CODE);
    }

    public static void startImagePicker(Fragment fragment) {
        Intent intent = new Intent();
        setIntentImagePicker(intent);
        fragment.startActivityForResult(
                Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_image_from)),
                GALLERY_REQUEST_CODE);
    }

    public static void startCameraPhotoForResult(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            return;
        }
        File photoFile = getTemporaryCameraFilePhoto();
        Uri uri = getValidUri(photoFile, activity);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, CAMERA_PHOTO_REQUEST_CODE);
    }

    public static void startCameraPhotoForResult(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) == null) {
            return;
        }
        File photoFile = getTemporaryCameraFilePhoto();
        Uri uri = getValidUri(photoFile, fragment.getContext());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        fragment.startActivityForResult(intent, CAMERA_PHOTO_REQUEST_CODE);
    }

    public static void startCameraVideoForResult(Activity activity) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(activity.getPackageManager()) == null) {
            return;
        }
        File videoFile = getTemporaryCameraFileVideo();
        Uri uri = getValidUri(videoFile, activity);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, ConstsCore.VIDEO_QUALITY_HIGH);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, ConstsCore.MAX_RECORD_DURATION_IN_SEC);
        activity.startActivityForResult(intent, CAMERA_VIDEO_REQUEST_CODE);
    }

    public static void startCameraVideoForResult(Fragment fragment) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (intent.resolveActivity(App.getInstance().getPackageManager()) == null) {
            return;
        }

        File videoFile = getTemporaryCameraFileVideo();
        Uri uri = getValidUri(videoFile, fragment.getContext());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, ConstsCore.VIDEO_QUALITY_HIGH);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, ConstsCore.MAX_RECORD_DURATION_IN_SEC);
        fragment.startActivityForResult(intent, CAMERA_VIDEO_REQUEST_CODE);
    }

    public static Uri getValidUri(File file, Context context) {
        Uri outputUri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            outputUri = FileProvider.getUriForFile(context, FileUtils.AUTHORITY, file);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            outputUri = Uri.fromFile(file);
        }
        return outputUri;
    }

    public static String getPathWithExtensionInLowerCase(String path) {
        String extensionInLowerCase = path.substring(path.lastIndexOf("."), path.length()).toLowerCase();
        String pathWithoutExtension = path.substring(0, path.lastIndexOf("."));
        return pathWithoutExtension + extensionInLowerCase;
    }

    public static void startMapForResult(Activity activity) {
        Intent intent = new Intent(activity, MapsActivity.class);
        activity.startActivityForResult(intent, IMAGE_VIDEO_LOCATION_REQUEST_CODE);
    }

    public static void startMapForResult(Fragment fragment) {
        Intent intent = new Intent(fragment.getContext(), MapsActivity.class);
        fragment.startActivityForResult(intent, IMAGE_VIDEO_LOCATION_REQUEST_CODE);
    }


    private static void setIntentMediaPicker(Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType(MimeType.IMAGE_MIME + MimeType.VIDEO_MIME_MP4 + MimeType.AUDIO_MIME_MP3);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, MimeType.mediaMimeTypes);
        }
    }

    private static void setIntentImagePicker(Intent intent) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType(MimeType.IMAGE_MIME);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType(MimeType.IMAGE_MIME);
        }
    }

    public static File getTemporaryCameraFilePhoto() {
        String fileName = CAMERA_FILE_NAME_PREFIX + DateUtilsCore.getCurrentTime() + CAMERA_PHOTO_FILE_EXT;
        return getTemporaryCameraFile(fileName);
    }

    public static File getTemporaryCameraFileVideo() {
        String fileName = CAMERA_FILE_NAME_PREFIX + DateUtilsCore.getCurrentTime() + CAMERA_VIDEO_FILE_EXT;
        return getTemporaryCameraFile(fileName);
    }

    public static File getTemporaryCameraFile(String fileName) {
        File storageDir = StorageUtil.getAppExternalDataDirectoryFile();
        File file = new File(storageDir, fileName);
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

    private static String getExtensionFromUri(Uri uri) {
        String mimeType = StringUtils.getMimeType(uri);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(mimeType);
    }

    public static String saveUriToFile(Uri uri) throws Exception {
        ParcelFileDescriptor parcelFileDescriptor = App.getInstance().getContentResolver()
                .openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        InputStream inputStream = new FileInputStream(fileDescriptor);
        BufferedInputStream bis = new BufferedInputStream(inputStream);

        File parentDir = StorageUtil.getAppExternalDataDirectoryFile();
        String path = uri.getPath();
        String extension = "";
        if (path.lastIndexOf(".") != -1) {
            extension = path.substring(path.lastIndexOf("."), path.length());
        } else {
            extension = "." + getExtensionFromUri(uri);
        }
        String fileName = String.valueOf(System.currentTimeMillis()) + extension;
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
                filePath = saveUriToFile(uri);
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
        Bitmap bitmap = getBitmapFromFile(imagePath);
        if (bitmap.getHeight() > bitmap.getWidth()) {
            rotateImage(bitmap, 90);
        }
    }

    private static Bitmap rotateImage(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotatedImg;
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

    /**
     * Allows to fix issue for some phones when image processed with android-crop
     * is not rotated properly.
     * Should be used in non-UI thread.
     */
    public static void normalizeRotationImageIfNeed(File file) {
        Context context = App.getInstance().getApplicationContext();
        String filePath = file.getPath();
        Uri uri = getValidUri(file, context);
        try {
            ExifInterface exif = new ExifInterface(filePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            Bitmap rotatedBitmap = rotateBitmap(bitmap, orientation);
            if (!bitmap.equals(rotatedBitmap)) {
                saveBitmapToFile(context, rotatedBitmap, uri);
            }
        } catch (Exception e) {
            ErrorUtils.logError("Exception:", e.getMessage());
        }
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();

            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void saveBitmapToFile(Context context, Bitmap croppedImage, Uri saveUri) {
        if (saveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = context.getContentResolver().openOutputStream(saveUri);
                if (outputStream != null) {
                    croppedImage.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                }
            } catch (IOException e) {
                ErrorUtils.logError("Cannot open file:", e.getMessage());
            } finally {
                closeSilently(outputStream);
                croppedImage.recycle();
            }
        }
    }

    private static void closeSilently(@Nullable Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
    }
}