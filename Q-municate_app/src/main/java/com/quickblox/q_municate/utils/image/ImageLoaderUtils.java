package com.quickblox.q_municate.utils.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.quickblox.q_municate.R;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

public class ImageLoaderUtils {

    public static final DisplayImageOptions UIL_DEFAULT_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).bitmapConfig(Bitmap.Config.RGB_565)
            .cacheOnDisc(true).cacheInMemory(true).build();

    public static final DisplayImageOptions UIL_USER_AVATAR_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_user).showImageForEmptyUri(R.drawable.placeholder_user)
            .showImageOnFail(R.drawable.placeholder_user).cacheOnDisc(true).cacheInMemory(true).build();

    public static final DisplayImageOptions UIL_GROUP_AVATAR_DISPLAY_OPTIONS = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.placeholder_group).showImageForEmptyUri(
                    R.drawable.placeholder_group).showImageOnFail(R.drawable.placeholder_group).cacheOnDisc(
                    true).cacheInMemory(true).build();

    public static ImageLoaderConfiguration getImageLoaderConfiguration(Context context) {
        final int MEMORY_CACHE_LIMIT = 2 * 1024 * 1024;
        final int THREAD_POOL_SIZE = 3;

        ImageLoaderConfiguration imageLoaderConfiguration = new ImageLoaderConfiguration.Builder(context)
                .threadPoolSize(THREAD_POOL_SIZE).threadPriority(Thread.NORM_PRIORITY)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new UsingFreqLimitedMemoryCache(MEMORY_CACHE_LIMIT))
                .defaultDisplayImageOptions(ImageLoaderUtils.UIL_DEFAULT_DISPLAY_OPTIONS)
                .imageDecoder(new SmartUriDecoder(context, new BaseImageDecoder(false)))
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new HashCodeFileNameGeneratorWithoutToken()).build();
        return imageLoaderConfiguration;
    }

    public static Bitmap getThumbnailFromVideo(String videoPath) {
        if (videoPath.contains("file://")) {
            videoPath = videoPath.replace("file://", "");
        }
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MINI_KIND);
    }

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
            Bitmap icon = BitmapFactory
                    .decodeResource(context.get().getResources(), R.drawable.ic_play_video_dark);

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