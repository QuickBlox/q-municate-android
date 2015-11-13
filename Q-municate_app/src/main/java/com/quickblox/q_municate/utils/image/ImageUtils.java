package com.quickblox.q_municate.utils.image;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
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
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.Display;

import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.MimeType;
import com.quickblox.q_municate.utils.SizeUtility;
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
import java.io.FileNotFoundException;
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

    private static final String CAMERA_FILE_NAME_PREFIX = "CAMERA_";
    private static final String CAMERA_FILE_NAME = CAMERA_FILE_NAME_PREFIX + DateUtilsCore.getCurrentTime() + ".jpg";



    public static final int GALLERY_INTENT_CALLED = 1;
    public static final int GALLERY_IMAGE_PREVIEWER_CALLED = 2;
    public static final int CAPTURE_CALLED = 3;

    private static final String TEMP_FILE_NAME = "temp.png";
    private static final int AVATAR_SIZE = 110;

    private Activity activity;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    //---


    public static byte[] getBytesFromBitmap(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, ConstsCore.FULL_QUALITY, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        Utils.closeOutputStream(byteArrayOutputStream);
        return byteArray;
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
        fragment.startActivityForResult(Intent.createChooser(intent, fragment.getString(R.string.dlg_choose_image_from)), GALLERY_REQUEST_CODE);
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
            e.printStackTrace();
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
        ParcelFileDescriptor parcelFileDescriptor = App.getInstance().getContentResolver().openFileDescriptor(
                uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        InputStream inputStream = new FileInputStream(fileDescriptor);
        BufferedInputStream bis = new BufferedInputStream(inputStream);

        File parentDir = StorageUtil.getAppExternalDataDirectoryFile();
        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
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

    // ---

    public boolean isGalleryCalled(int requestCode) {
        return ImageUtils.GALLERY_INTENT_CALLED == requestCode;
    }

    public boolean isCaptureCalled(int requestCode) {
        return ImageUtils.CAPTURE_CALLED == requestCode;
    }

    public static byte[] getBytesBitmap(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, ConstsCore.FULL_QUALITY, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        Utils.closeOutputStream(byteArrayOutputStream);
        return byteArray;
    }

    public Bitmap createScaledBitmap(Bitmap unscaledBitmap) {
        Display display = activity.getWindowManager().getDefaultDisplay();
        int displayWidth = display.getWidth();

        Bitmap scaledBitmap = createScaledBitmap(unscaledBitmap, displayWidth, displayWidth, ScalingLogic.FIT);

        return scaledBitmap;
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
                return new Rect(srcRectLeft, ConstsCore.ZERO_INT_VALUE, srcRectLeft + srcRectWidth, srcHeight);
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
                return new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, (int) (dstHeight * srcAspect),
                        dstHeight);
            }
        } else {
            return new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, dstWidth, dstHeight);
        }
    }

    public void getCaptureImage() {
        Intent cameraIntent=new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(cameraIntent, CAPTURE_CALLED);
    }

    public void getImage() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            activity.startActivityForResult(intent, GALLERY_INTENT_CALLED);
        } else {
            showKitKatGallery();
        }
    }

    private void showKitKatGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, GALLERY_INTENT_CALLED);
    }

    public void showFullImage(Activity activity, String absolutePath) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        Uri uri = Uri.parse("file://" + absolutePath);
        intent.setDataAndType(uri, "image/*");
        activity.startActivityForResult(intent, GALLERY_IMAGE_PREVIEWER_CALLED);
    }

    public Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap resultBitmap;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float r;

        if (originalWidth > originalHeight) {
            resultBitmap = Bitmap.createBitmap(originalHeight, originalHeight, Bitmap.Config.ARGB_8888);
            r = originalHeight / 2;
        } else {
            resultBitmap = Bitmap.createBitmap(originalWidth, originalWidth, Bitmap.Config.ARGB_8888);
            r = originalWidth / 2;
        }

        Canvas canvas = new Canvas(resultBitmap);

        final Paint paint = new Paint();
        final Rect rect = new Rect(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, originalWidth, originalHeight);

        paint.setAntiAlias(true);
        canvas.drawARGB(ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE, ConstsCore.ZERO_INT_VALUE);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return resultBitmap;
    }

    public String getAbsolutePathByBitmap(Bitmap origBitmap) {
        File tempFile = new File(activity.getExternalFilesDir(null), TEMP_FILE_NAME);
        ByteArrayOutputStream bos = null;
        FileOutputStream fos = null;
        try {
            bos = new ByteArrayOutputStream();
            origBitmap.compress(Bitmap.CompressFormat.PNG, ConstsCore.FULL_QUALITY, bos);
            byte[] bitmapData = bos.toByteArray();
            fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            Utils.closeOutputStream(fos);
            Utils.closeOutputStream(bos);
        } catch (IOException e) {
            ErrorUtils.showError(activity, e);
        } finally {
            Utils.closeOutputStream(fos);
            Utils.closeOutputStream(bos);
        }
        return tempFile.getAbsolutePath();
    }

//    public File getFileFromBitmapForCamera(Bitmap origBitmap) throws IOException {
////        origBitmap = rotateImageIfRequired(App.getInstance(), origBitmap);
//        File tempFile = getFileFromBitmap(origBitmap);
//        return tempFile;
//    }

    public Uri getImageUri(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public File getFileFromBitmap(Bitmap origBitmap) throws IOException {
        int width = SizeUtility.dipToPixels(activity, ConstsCore.CHAT_ATTACH_WIDTH);
        int height = SizeUtility.dipToPixels(activity, ConstsCore.CHAT_ATTACH_HEIGHT);
        Bitmap bitmap = createScaledBitmap(origBitmap, width, height, ScalingLogic.FIT);
        byte[] bitmapData = getBytesBitmap(bitmap);
        File tempFile = createFile(bitmapData);
        return tempFile;
    }

    public File createFile(byte[] bitmapData) throws IOException {
        File tempFile = new File(activity.getCacheDir(), TEMP_FILE_NAME);
        tempFile.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        fileOutputStream.write(bitmapData);
        Utils.closeOutputStream(fileOutputStream);
        return tempFile;
    }

    public Bitmap getBitmap(Uri originalUri) {
        BitmapFactory.Options bitmapOptions = getBitmapOption();
        Bitmap selectedBitmap = null;
        try {
            ParcelFileDescriptor descriptor = activity.getContentResolver().openFileDescriptor(originalUri, "r");
            selectedBitmap = BitmapFactory.decodeFileDescriptor(descriptor.getFileDescriptor(), null, bitmapOptions);
        } catch (FileNotFoundException e) {
            ErrorUtils.showError(activity, e.getMessage());
        }
        return selectedBitmap;
    }

    private void rotateImageIfRequired(Bitmap bitmap, Uri uri) {
        try {
            ExifInterface exifInterface = new ExifInterface(getRealPathFromURI(uri));
            if (exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")) {
                bitmap = rotate(bitmap, 90);
            } else if (exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")) {
                bitmap = rotate(bitmap, 270);
            } else if (exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")) {
                bitmap = rotate(bitmap, 180);
            }
        } catch (IOException e) {
            ErrorUtils.logError(e);
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(columnIndex);
        }
    }

    private Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    private BitmapFactory.Options getBitmapOption() {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inDither = false;
        bitmapOptions.inPurgeable = true;
        bitmapOptions.inInputShareable = true;
        bitmapOptions.inTempStorage = new byte[32 * 1024];
        return bitmapOptions;
    }

    public static Drawable getRoundIconDrawable(Context context, int imageResId) {
        int actionBarHeight = getActionBarHeight(context);
        Bitmap avatarBitmap = BitmapFactory.decodeResource(context.getResources(), imageResId);
        return getRoundIconDrawable(avatarBitmap, actionBarHeight);
    }

    public static Drawable getRoundIconDrawable(Context context, Bitmap avatarBitmap) {
        int actionBarHeight = getActionBarHeight(context);
        return getRoundIconDrawable(avatarBitmap, actionBarHeight);
    }

    private static int getActionBarHeight(Context context) {
        TypedValue typedValue = new TypedValue();
        int actionBarHeight = AVATAR_SIZE;

        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(typedValue.data,
                    context.getResources().getDisplayMetrics());
        }

        int margin = actionBarHeight / 10;

        return actionBarHeight - margin;
    }

    private static Drawable getRoundIconDrawable(Bitmap avatarBitmap, int size) {
        // TODO Remove freaking hardcoded values
        Resources res = App.getInstance().getResources();
        Bitmap scaledBitmap = createScaledBitmap(avatarBitmap, size, size, ScalingLogic.CROP);

        // create rounded image avatar
        Bitmap output = Bitmap.createBitmap(scaledBitmap.getWidth(),
                scaledBitmap.getHeight(), Bitmap.Config.ARGB_8888);
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