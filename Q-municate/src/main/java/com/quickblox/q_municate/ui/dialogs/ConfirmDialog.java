package com.quickblox.q_municate.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.quickblox.q_municate.utils.DialogUtils;

public class ConfirmDialog extends DialogFragment {

    private int titleId;
    private int messageId;
    private DialogInterface.OnClickListener positiveButtonListener;
    private DialogInterface.OnClickListener negativeButtonListener;

    public static ConfirmDialog newInstance(int titleId, int messageId) {
        return new ConfirmDialog(titleId, messageId);
    }

    public ConfirmDialog(int titleId, int messageId) {
        this.titleId = titleId;
        this.messageId = messageId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return DialogUtils.createDialog(getActivity(), titleId, messageId, positiveButtonListener, negativeButtonListener);
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setPositiveButton(final DialogInterface.OnClickListener listener) {
        positiveButtonListener = listener;
    }

    public void setNegativeButton(final DialogInterface.OnClickListener listener) {
        negativeButtonListener = listener;
    }
}