package com.quickblox.q_municate.utils;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;

public class DialogsUtils {

    public static final long OPEN_APP_SETTINGS_DIALOG_DELAY = 500;

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

    public static void showOpenAppSettingsDialog(FragmentManager fm, final String dialogMessage, final MaterialDialog.ButtonCallback callback) {
        //postDelayed() is temp fix before fixing this bug https://code.google.com/p/android/issues/detail?id=190966
        TwoButtonsDialogFragment.showDelayed(
                fm,
                App.getInstance().getString(R.string.app_name),
                dialogMessage,
                App.getInstance().getString(R.string.dlg_ok),
                App.getInstance().getString(R.string.dlg_open_app_settings),
                callback,
                OPEN_APP_SETTINGS_DIALOG_DELAY);
    }
}