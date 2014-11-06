package com.quickblox.q_municate.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.quickblox.q_municate_core.utils.DialogUtils;

public class ConfirmDialog extends DialogFragment {

    private static final String ARGS_TITLE_ID = "title_id";
    private static final String ARGS_MESSAGE_ID = "message_id";

    private DialogInterface.OnClickListener positiveButtonListener;
    private DialogInterface.OnClickListener negativeButtonListener;

    public static ConfirmDialog newInstance(int titleId, int messageId) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        Bundle args = new Bundle();
        args.putInt(ARGS_TITLE_ID, titleId);
        args.putInt(ARGS_MESSAGE_ID, messageId);
        confirmDialog.setArguments(args);
        return confirmDialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int titleId = getArguments().getInt(ARGS_TITLE_ID);
        int messageId = getArguments().getInt(ARGS_MESSAGE_ID);
        return DialogUtils.createDialog(getActivity(), titleId, messageId, positiveButtonListener, negativeButtonListener);
    }

    public void setPositiveButton(final DialogInterface.OnClickListener listener) {
        positiveButtonListener = listener;
    }

    public void setNegativeButton(final DialogInterface.OnClickListener listener) {
        negativeButtonListener = listener;
    }
}