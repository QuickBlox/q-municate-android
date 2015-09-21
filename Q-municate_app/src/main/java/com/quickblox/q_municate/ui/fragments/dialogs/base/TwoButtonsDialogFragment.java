package com.quickblox.q_municate.ui.fragments.dialogs.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;

public class TwoButtonsDialogFragment extends DialogFragment {

    private static final String TAG = TwoButtonsDialogFragment.class.getSimpleName();

    private static final String ARG_TITLE = "title";
    private static final String ARG_CONTENT = "content";

    private String title;
    private String content;
    private MaterialDialog.ButtonCallback buttonsCallback;

    public static void show(FragmentManager fm, String title, String message, MaterialDialog.ButtonCallback callback) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, message);

        TwoButtonsDialogFragment twoButtonsDialog = new TwoButtonsDialogFragment();
        twoButtonsDialog.setCallbacks(callback);
        twoButtonsDialog.setArguments(args);
        twoButtonsDialog.show(fm, TAG);
    }

    public static void show(FragmentManager fm, String message, MaterialDialog.ButtonCallback callback) {
        show(fm, null, message, callback);
    }

    public static void show(FragmentManager fm, int message, MaterialDialog.ButtonCallback callback) {
        show(fm, null, App.getInstance().getString(message), callback);
    }

    public static void show(FragmentManager fm, int message) {
        show(fm, null, App.getInstance().getString(message), null);
    }

    public static void show(FragmentManager fm, int title, int message, MaterialDialog.ButtonCallback callback) {
        show(fm, App.getInstance().getString(title), App.getInstance().getString(message), callback);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        title = getArguments().getString(ARG_TITLE);
        content = getArguments().getString(ARG_CONTENT);
        return createDialog();
    }

    private MaterialDialog createDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .positiveText(R.string.dlg_ok)
                .negativeText(R.string.dlg_cancel)
                .callback(buttonsCallback);

        if (!TextUtils.isEmpty(title)) {
            builder.title(title);
        }

        if (!TextUtils.isEmpty(content)) {
            builder.content(content);
        }

        return builder.build();
    }

    public void setCallbacks(MaterialDialog.ButtonCallback callback) {
        this.buttonsCallback = callback;
    }
}