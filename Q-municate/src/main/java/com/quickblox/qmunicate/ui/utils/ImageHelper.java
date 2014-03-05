package com.quickblox.qmunicate.ui.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ImageHelper {
    public static final int GALLERY_KITKAT_INTENT_CALLED = 2;
    public static final int GALLERY_INTENT_CALLED = 1;
    private Activity activity;
    private String pathToImage;

    public ImageHelper(Activity activity) {
        this.activity = activity;
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
        activity.startActivityForResult(intent, GALLERY_KITKAT_INTENT_CALLED);
    }

    public String getPath(Uri uri) {
        if (uri == null) {
            return null;
        }
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        return uri.getPath();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public String getPath(Uri originalUri, int flags) {
        final int takeFlags = flags & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.getContentResolver().takePersistableUriPermission(originalUri, takeFlags);
        String id = originalUri.getLastPathSegment().split(":")[1];
        final String[] imageColumns = {MediaStore.Images.Media.DATA};
        final String imageOrderBy = null;
        Uri uri = getUri();
        Cursor imageCursor = activity.managedQuery(uri, imageColumns,
                MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);
        if (imageCursor.moveToFirst()) {
            pathToImage = imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
        return pathToImage;
    }

    public Uri getUri() {
        String state = Environment.getExternalStorageState();
        if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED))
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public boolean equalsBitmaps(Bitmap bitmap1, Bitmap bitmap2) {
        ByteBuffer buffer1 = ByteBuffer.allocate(bitmap1.getHeight() * bitmap1.getRowBytes());
        bitmap1.copyPixelsToBuffer(buffer1);

        ByteBuffer buffer2 = ByteBuffer.allocate(bitmap2.getHeight() * bitmap2.getRowBytes());
        bitmap2.copyPixelsToBuffer(buffer2);

        return Arrays.equals(buffer1.array(), buffer2.array());
    }
}