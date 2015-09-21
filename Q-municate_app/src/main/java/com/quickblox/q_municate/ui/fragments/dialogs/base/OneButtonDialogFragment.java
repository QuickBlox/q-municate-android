package com.quickblox.q_municate.ui.fragments.dialogs.base;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.DialogsUtils;

public class OneButtonDialogFragment extends DialogFragment {

    private static final String TAG = OneButtonDialogFragment.class.getSimpleName();
    private static final String ARG_CONTENT = "content";
    private static final String ARG_CANCELABLE = "cancelable";

    private String content;
    private MaterialDialog.ButtonCallback callback;

    public static void show(FragmentManager fm, String content, boolean cancelable, MaterialDialog.ButtonCallback callback) {
        OneButtonDialogFragment oneButtonDialogFragment = new OneButtonDialogFragment();

        Bundle args = new Bundle();
        args.putString(ARG_CONTENT, content);
        args.putBoolean(ARG_CANCELABLE, cancelable);
        oneButtonDialogFragment.setArguments(args);
        oneButtonDialogFragment.setCallbacks(callback);

        oneButtonDialogFragment.show(fm, TAG);
    }

    public static void show(FragmentManager fm, int content, boolean cancelable, MaterialDialog.ButtonCallback callback) {
        show(fm, App.getInstance().getString(content), cancelable, callback);
    }

    public static void show(FragmentManager fm, int content, boolean cancelable) {
        show(fm, App.getInstance().getString(content), cancelable, null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        content = getArguments().getString(ARG_CONTENT);
        return createDialog();
    }

    private MaterialDialog createDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .positiveText(R.string.dlg_ok)
                .callback(callback);

        if (!TextUtils.isEmpty(content)) {
            builder.content(content);
        }

        MaterialDialog materialDialog = builder.build();

        if (!getArguments().getBoolean(ARG_CANCELABLE)) {
            DialogsUtils.disableCancelableDialog(materialDialog);
        }

        return materialDialog;
    }

    public void setCallbacks(MaterialDialog.ButtonCallback callback) {
        this.callback = callback;
    }
}