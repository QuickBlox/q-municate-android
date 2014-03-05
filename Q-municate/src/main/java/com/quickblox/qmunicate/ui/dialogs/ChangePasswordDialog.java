package com.quickblox.qmunicate.ui.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
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
        return new ChangePasswordDialog();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        oldPassword = (EditText) rootView.findViewById(R.id.oldPassword);
        newPassword = (EditText) rootView.findViewById(R.id.newPassword);
        confirmPassword = (EditText) rootView.findViewById(R.id.confirmPassword);

        return DialogUtils.createDialog(getActivity(), R.string.cpd_title, rootView,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changePassword();
                    }
                }, null);
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
