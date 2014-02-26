package com.quickblox.qmunicate.ui.utils;

import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

public class ErrorUtils {

    public static void showError(FragmentActivity activity, Exception e) {
        Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT);
    }
}
