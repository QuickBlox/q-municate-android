package com.quickblox.qmunicate.ui.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ErrorUtils {

    public static void showError(Context context, Exception e) {
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public static void showError(Context context, String error) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
    }

    public static void logError(String tag, Exception e) {
        Log.e(tag, e.getMessage());
    }

    public static void logError(Exception e) {
        e.printStackTrace();
    }

}
