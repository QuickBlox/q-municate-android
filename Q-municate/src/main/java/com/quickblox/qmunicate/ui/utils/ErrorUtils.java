package com.quickblox.qmunicate.ui.utils;

import android.app.Activity;
import android.widget.Toast;

public class ErrorUtils {

    public static void showError(Activity activity, Exception e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT);
    }
}
