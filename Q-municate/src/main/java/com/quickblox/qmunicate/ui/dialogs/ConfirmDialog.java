package com.quickblox.qmunicate.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.quickblox.qmunicate.R;

public class ConfirmDialog extends DialogFragment {

    private int titleId;
    private int messageId;
    private OnConfirmDialogClickListener onConfirmDialogClickListener;

    public static ConfirmDialog newInstance(int titleId, int messageId) {
        return new ConfirmDialog(titleId, messageId);
    }

    public ConfirmDialog(int titleId, int messageId) {
        this.titleId = titleId;
        this.messageId = messageId;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirmDialogClickListener.onOkButtonClick();
            }
        });
        builder.setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onConfirmDialogClickListener.onCancelButtonClick();
            }
        });

        return builder.create();
    }

    public void setOnConfirmDialogClickListener(OnConfirmDialogClickListener onConfirmDialogClickListener) {
        this.onConfirmDialogClickListener = onConfirmDialogClickListener;
    }

    public interface OnConfirmDialogClickListener {

        public void onOkButtonClick();

        public void onCancelButtonClick();
    }
}