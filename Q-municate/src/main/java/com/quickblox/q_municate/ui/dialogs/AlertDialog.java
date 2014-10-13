package com.quickblox.q_municate.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.quickblox.q_municate.utils.DialogUtils;

public class AlertDialog extends DialogFragment {

    private String message;
    private DialogInterface.OnClickListener positiveButtonListener;
    private DialogInterface.OnClickListener negativeButtonListener;

    public static AlertDialog newInstance(String message) {
        return new AlertDialog(message);
    }

    public AlertDialog(String message) {
        this.message = message;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return DialogUtils.createDialog(getActivity(), message, positiveButtonListener,
                negativeButtonListener);
    }

    public void setMessageId(String message) {
        this.message = message;
    }

    public void setPositiveButton(final DialogInterface.OnClickListener listener) {
        positiveButtonListener = listener;
    }

    public void setNegativeButton(final DialogInterface.OnClickListener listener) {
        negativeButtonListener = listener;
    }
}