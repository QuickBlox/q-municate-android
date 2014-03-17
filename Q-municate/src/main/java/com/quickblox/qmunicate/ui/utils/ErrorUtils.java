package com.quickblox.qmunicate.ui.utils;

import android.content.Context;
import android.widget.Toast;

public class ErrorUtils {

    public static void showError(Context context, Exception e) {
        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT);
    }
}
