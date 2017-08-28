package com.quickblox.q_municate.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.q_municate.BuildConfig;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    private static final String folderName = "/Q-municate";
    private static final String fileType = ".jpg";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private File filesFolder;

    public FileUtils() {
        initFilesFolder();
    }

    private void initFilesFolder() {
        filesFolder = new File(Environment.getExternalStorageDirectory() + folderName);
        if (!filesFolder.exists()) {
            filesFolder.mkdirs();
        }
    }

    public void checkExistsFile(String fileUrlString, Bitmap bitmap) {
        Log.d(TAG, "+++ fileUrlString = " + fileUrlString);
        if (!TextUtils.isEmpty(fileUrlString)) {
            File file = createFileIfNotExist(fileUrlString);
            if (!file.exists()) {
                saveFile(file, bitmap);
            }
        }
    }

    public File createFileIfNotExist(String fileUrlString) {
        Uri fileUri = Uri.parse(fileUrlString);
        String fileName = fileUri.getLastPathSegment() + fileType;
        return new File(filesFolder, fileName);
    }

    private void saveFile(File file, Bitmap bitmap) {
        // starting new Async Task
        new SavingFileTask().execute(file, bitmap);
    }

    private class SavingFileTask extends AsyncTask<Object, String, Object> {

        @Override
        protected Object doInBackground(Object... objects) {
            File file = (File) objects[0];
            Bitmap bitmap = (Bitmap) objects[1];

            FileOutputStream fileOutputStream;

            try {
                fileOutputStream = new FileOutputStream(file);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, ConstsCore.ZERO_INT_VALUE, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                fileOutputStream.write(byteArray);

                fileOutputStream.flush();
                fileOutputStream.close();

                byteArrayOutputStream.flush();
                byteArrayOutputStream.close();

            } catch (FileNotFoundException e) {
                ErrorUtils.logError(e);
            } catch (IOException e) {
                ErrorUtils.logError(e);
            }

            return null;
        }
    }
}