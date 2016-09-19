package com.quickblox.q_municate.ui.fragments.dialogs.base;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
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
    private static final String ARG_POSITIVE_TEXT = "positiveText";
    private static final String ARG_NEGATIVE_TEXT = "negativeText";

    private String title;
    private String content;
    private String positiveText;
    private String negativeText;
    private MaterialDialog.ButtonCallback buttonsCallback;

    public static void show(FragmentManager fm, String title, String message, String positiveText, String negativeText, MaterialDialog.ButtonCallback callback) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_CONTENT, message);
        args.putString(ARG_POSITIVE_TEXT, positiveText);
        args.putString(ARG_NEGATIVE_TEXT, negativeText);


        TwoButtonsDialogFragment twoButtonsDialog = new TwoButtonsDialogFragment();
        twoButtonsDialog.setCallbacks(callback);
        twoButtonsDialog.setArguments(args);
        twoButtonsDialog.show(fm, TAG);
    }

    public static void show(FragmentManager fm, String message, MaterialDialog.ButtonCallback callback) {
        show(fm, null, message, null, null, callback);
    }

    public static void show(FragmentManager fm, int message, MaterialDialog.ButtonCallback callback) {
        show(fm, null, App.getInstance().getString(message), null, null, callback);
    }

    public static void show(FragmentManager fm, int message) {
        show(fm, null, App.getInstance().getString(message), null, null, null);
    }

    public static void show(FragmentManager fm, int title, int message, MaterialDialog.ButtonCallback callback) {
        show(fm, App.getInstance().getString(title), App.getInstance().getString(message), null, null, callback);
    }

    public static void show(FragmentManager fm, int title, int message,
                            int positiveText, int negativeText, MaterialDialog.ButtonCallback callback) {
        show(fm, App.getInstance().getString(title), App.getInstance().getString(message),
                App.getInstance().getString(positiveText), App.getInstance().getString(negativeText), callback);
    }

    public static void showDelayed(final FragmentManager fm, final String title, final String message,
                                   final String positiveText, final String negativeText, final MaterialDialog.ButtonCallback callback, long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                show(fm,
                        title,
                        message,
                        positiveText,
                        negativeText,
                        callback);
            }
        }, delay);
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        title = getArguments().getString(ARG_TITLE);
        content = getArguments().getString(ARG_CONTENT);
        positiveText = getArguments().getString(ARG_POSITIVE_TEXT);
        negativeText = getArguments().getString(ARG_NEGATIVE_TEXT);
        return createDialog();
    }

    private MaterialDialog createDialog() {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity())
                .callback(buttonsCallback);

        if (!TextUtils.isEmpty(title)) {
            builder.title(title);
        }

        if (!TextUtils.isEmpty(positiveText)) {
            builder.positiveText(positiveText);
        } else {
            builder.positiveText(R.string.dlg_ok);
        }

        if (!TextUtils.isEmpty(negativeText)) {
            builder.negativeText(negativeText);
        } else {
            builder.negativeText(R.string.dlg_cancel);
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