package com.quickblox.q_municate.utils;

import android.content.DialogInterface;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.MaterialDialog;

public class DialogsUtils {

    public static void disableCancelableDialog(MaterialDialog materialDialog) {
        // Disable the back button
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        };
        materialDialog.setOnKeyListener(keyListener);

        materialDialog.setCanceledOnTouchOutside(false);
    }
}