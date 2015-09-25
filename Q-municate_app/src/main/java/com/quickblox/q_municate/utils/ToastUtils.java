package com.quickblox.q_municate.utils;

import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.quickblox.q_municate.App;

public class ToastUtils {

    public static void shortToast(@StringRes int text) {
        shortToast(App.getInstance().getString(text));
    }

    public static void shortToast(String text) {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void longToast(@StringRes int text) {
        longToast(App.getInstance().getString(text));
    }

    public static void longToast(String text) {
        show(text, Toast.LENGTH_LONG);
    }

    private static Toast makeToast(String text, @ToastLength int length) {
        return Toast.makeText(App.getInstance(), text, length);
    }

    private static void show(String text, @ToastLength int length) {
        makeToast(text, length).show();
    }

    @IntDef({ Toast.LENGTH_LONG, Toast.LENGTH_SHORT })
    private @interface ToastLength {

    }
}