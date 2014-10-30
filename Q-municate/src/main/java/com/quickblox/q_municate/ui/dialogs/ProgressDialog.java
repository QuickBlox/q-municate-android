package com.quickblox.q_municate.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

public class ProgressDialog extends DialogFragment {

    private static final String ARGS_MESSAGE_ID = "message_id";

    public static ProgressDialog newInstance(int messageId) {
        ProgressDialog progressDialog = new ProgressDialog();
        Bundle args = new Bundle();
        args.putInt(ARGS_MESSAGE_ID, messageId);
        progressDialog.setArguments(args);
        return progressDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int messageId = getArguments().getInt(ARGS_MESSAGE_ID);
        android.app.ProgressDialog dialog = new android.app.ProgressDialog(getActivity());
        dialog.setMessage(getString(messageId));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);

        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return keyCode == KeyEvent.KEYCODE_BACK;
            }
        };
        dialog.setOnKeyListener(keyListener);

        return dialog;
    }
}