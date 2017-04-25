package com.quickblox.q_municate.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.fragments.dialogs.base.TwoButtonsDialogFragment;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;

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
                App.getInstance().getString(R.string.dlg_cancel),
                App.getInstance().getString(R.string.dlg_open_app_settings),
                callback,
                OPEN_APP_SETTINGS_DIALOG_DELAY);
    }

    public static void loadAllDialogsFromCacheByPages(Context context, long dialogsSize) {
        boolean needToLoadMore;
        boolean update = true;

        int startRow = 0;
        int perPage = ConstsCore.CHATS_DIALOGS_PER_PAGE;

        do {
            needToLoadMore = dialogsSize > ConstsCore.CHATS_DIALOGS_PER_PAGE;

            if(!needToLoadMore){
                perPage = (int) dialogsSize;
            }

            Bundle bundle = new Bundle();
            bundle.putInt(ConstsCore.DIALOGS_START_ROW, startRow);
            bundle.putInt(ConstsCore.DIALOGS_PER_PAGE, perPage);
            bundle.putBoolean(ConstsCore.DIALOGS_NEED_UPDATE, update);
            update = false;

            sendLoadPageSuccess(context, bundle);
            dialogsSize -= perPage;

            startRow += perPage;
        } while (needToLoadMore);
    }

    public static void sendLoadPageSuccess(Context context, Bundle result) {
        Intent intent = new Intent(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION);
        if (null != result) {
            intent.putExtras(result);
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}