package com.quickblox.q_municate.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Display;
import android.webkit.MimeTypeMap;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.ErrorUtils;
import com.quickblox.q_municate_core.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class ImageUtils {

    public static final int GALLERY_INTENT_CALLED = 1;
    public static final int GALLERY_IMAGE_PREVIEWER_CALLED = 2;
    public static final int CAPTURE_CALLED = 3;

    private static final String TEMP_FILE_NAME = "temp.png";

    private Activity activity;

    public ImageUtils(Activity activity) {
        this.activity = activity;
    }

    public static ImageLoaderConfiguration getImageLoaderConfiguration(Context context) {
        final int MEMORY_CACHE_LIMIT = 2 * 1024 * 1024;
        final int THREAD_POOL_SIZE = 5;

        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(THREAD_POOL_SIZE)
                .threadPriority(Thread.NORM_PRIORITY).denyCacheImageMultipleSizesInMemory().memoryCache(
                        new UsingFreqLimitedMemoryCache(MEMORY_CACHE_LIMIT)).writeDebugLogs()
                .defaultDisplayImageOptions(Consts.UIL_DEFAULT_DISPLAY_OPTIONS).imageDecoder(
                        new SmartUriDecoder(context, new BaseImageDecoder(false)))
                .denyCacheImageMultipleSizesInMemory().discCacheFileNameGenerator(
                        new HashCodeFileNameGeneratorWithoutToken()).build();
        return imageLoaderConfiguration;
    }

    public static Bitmap getThumbnailFromVideo(String videoPath) {
        if (videoPath.contains("file://")) {
            videoPath = videoPath.replace("file://", "");
        }
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
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

    private Bitmap createScaledBitmap(Bitmap unscaledBitmap, int dstWidth, int dstHeight,
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

    private Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
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

    public Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
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

    private BitmapFactory.Options getBitmapOption() {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inDither = false;
        bitmapOptions.inPurgeable = true;
        bitmapOptions.inInputShareable = true;
        bitmapOptions.inTempStorage = new byte[32 * 1024];
        return bitmapOptions;
    }

    private enum ScalingLogic {
        CROP, FIT
    }

    /*
    * TODO Sergey Fedunets: class will be realised for video attach
     */
    public static class SmartUriDecoder implements ImageDecoder {

        private final BaseImageDecoder imageUriDecoder;

        private final Reference<Context> context;

        public SmartUriDecoder(Context context, BaseImageDecoder imageUriDecoder) {
            if (imageUriDecoder == null) {
                throw new NullPointerException("Image decoder can't be null");
            }

            this.context = new WeakReference(context);
            this.imageUriDecoder = imageUriDecoder;
        }

        @Override
        public Bitmap decode(ImageDecodingInfo info) throws IOException {
            if (TextUtils.isEmpty(info.getImageKey())) {
                return null;
            }

            String cleanedUriString = cleanUriString(info.getImageKey());
            if (isVideoUri(cleanedUriString)) {
                return makeVideoThumbnail(info.getTargetSize().getWidth(), info.getTargetSize().getHeight(),
                        cleanedUriString);
            } else {
                return imageUriDecoder.decode(info);
            }
        }

        private Bitmap makeVideoThumbnail(int width, int height, String filePath) {
            if (filePath == null) {
                return null;
            }
            Bitmap thumbnail = getThumbnailFromVideo(filePath);
            if (thumbnail == null) {
                return null;
            }

            Bitmap scaledThumb = scaleBitmap(thumbnail, width, height);
            thumbnail.recycle();

            addVideoIcon(scaledThumb);
            return scaledThumb;
        }

        private void addVideoIcon(Bitmap source) {
            Canvas canvas = new Canvas(source);
            Bitmap icon = BitmapFactory.decodeResource(context.get().getResources(), R.drawable.video_icon);

            float left = (source.getWidth() / 2) - (icon.getWidth() / 2);
            float top = (source.getHeight() / 2) - (icon.getHeight() / 2);

            canvas.drawBitmap(icon, left, top, null);
        }

        private boolean isVideoUri(String uri) {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);

            return mimeType == null ? false : mimeType.startsWith("video/");
        }

        private Bitmap scaleBitmap(Bitmap origBitmap, int width, int height) {
            float scale = Math.min(((float) width) / ((float) origBitmap.getWidth()),
                    ((float) height) / ((float) origBitmap.getHeight()));
            return Bitmap.createScaledBitmap(origBitmap, (int) (((float) origBitmap.getWidth()) * scale),
                    (int) (((float) origBitmap.getHeight()) * scale), false);
        }

        private String cleanUriString(String contentUriWithAppendedSize) {
            return contentUriWithAppendedSize.replaceFirst("_\\d+x\\d+$", "");
        }
    }

    private static class HashCodeFileNameGeneratorWithoutToken extends HashCodeFileNameGenerator {

        private static final String FACEBOOK_PATTERN = "https://graph.facebook.com/";
        private static final String TOKEN_PATTERN = "\\?token+=+.*";

        @Override
        public String generate(String imageUri) {
            if (imageUri.contains(FACEBOOK_PATTERN)) {
                return imageUri;
            }
            String replace = imageUri.replaceAll(TOKEN_PATTERN, "");
            return super.generate(replace);
        }
    }
}