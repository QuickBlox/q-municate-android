package com.quickblox.qmunicate.ui.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBUpdateUserTask;
import com.quickblox.qmunicate.ui.utils.DialogUtils;

public class ChangePasswordDialog extends DialogFragment {

    private EditText oldPassword;
    private EditText newPassword;
    private EditText confirmPassword;

    public static ChangePasswordDialog newInstance() {
        ChangePasswordDialog dialog = new ChangePasswordDialog();
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        oldPassword = (EditText) rootView.findViewById(R.id.oldPassword);
        newPassword = (EditText) rootView.findViewById(R.id.newPassword);
        confirmPassword = (EditText) rootView.findViewById(R.id.confirmPassword);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.cpd_title);
        builder.setView(rootView);
        builder.setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changePassword();
            }
        });
        builder.setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        return builder.create();
    }

    private void changePassword() {
        String oldPasswordText = oldPassword.getText().toString();
        String newPasswordText = newPassword.getText().toString();
        String confirmPasswordText = confirmPassword.getText().toString();

        boolean isOldPasswordEmpty = TextUtils.isEmpty(oldPasswordText);
        boolean isNewPasswordEmpty = TextUtils.isEmpty(newPasswordText);
        boolean isConfirmPasswordEmpty = TextUtils.isEmpty(confirmPasswordText);

        if (isOldPasswordEmpty || isNewPasswordEmpty || isConfirmPasswordEmpty) {
            DialogUtils.show(getActivity(), getString(R.string.dlg_not_all_fields_entered));
            return;
        }

        if (!newPasswordText.equals(confirmPasswordText)) {
            DialogUtils.show(getActivity(), getString(R.string.dlg_passwords_not_equal));
            return;
        }

        QBUser user = App.getInstance().getUser();
        user.setOldPassword(oldPasswordText);
        user.setPassword(newPasswordText);
        new QBUpdateUserTask(getActivity()).execute(user);
    }
}
