package com.quickblox.q_municate.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.quickblox.q_municate_core.utils.DialogUtils;

public class AlertDialog extends DialogFragment {

    private static final String ARGS_MESSAGE = "message";

    private DialogInterface.OnClickListener positiveButtonListener;
    private DialogInterface.OnClickListener negativeButtonListener;

    public static AlertDialog newInstance(String message) {
        AlertDialog alertDialog = new AlertDialog();
        Bundle args = new Bundle();
        args.putString(ARGS_MESSAGE, message);
        alertDialog.setArguments(args);
        return alertDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString(ARGS_MESSAGE);
        return DialogUtils.createDialog(getActivity(), message, positiveButtonListener,
                negativeButtonListener);
    }

    public void setPositiveButton(final DialogInterface.OnClickListener listener) {
        positiveButtonListener = listener;
    }

    public void setNegativeButton(final DialogInterface.OnClickListener listener) {
        negativeButtonListener = listener;
    }
}