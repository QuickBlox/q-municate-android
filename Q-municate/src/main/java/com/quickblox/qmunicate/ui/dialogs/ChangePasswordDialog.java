package com.quickblox.qmunicate.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.commands.QBChangePasswordCommand;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.utils.DialogUtils;

public class ChangePasswordDialog extends DialogFragment {

    private BaseActivity activity;

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
        activity = (BaseActivity) getActivity();
        return DialogUtils.createDialog(getActivity(), R.string.cpd_title, rootView,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changePassword();
                    }
                }, null
        );
    }

    private void changePassword() {
        String oldPasswordText = oldPassword.getText().toString();
        String newPasswordText = newPassword.getText().toString();


        QBUser user = App.getInstance().getUser();
        user.setOldPassword(oldPasswordText);
        user.setPassword(newPasswordText);
        activity.showProgress();
        QBChangePasswordCommand.start(activity, user);
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog dialog = (AlertDialog)getDialog();
        final Button yesButton = dialog == null ? null : dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if(yesButton != null){
            yesButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    String oldPasswordText = oldPassword.getText().toString();
                    String newPasswordText = newPassword.getText().toString();
                    String confirmPasswordText = confirmPassword.getText().toString();

                    boolean isOldPasswordEmpty = TextUtils.isEmpty(oldPasswordText);
                    boolean isNewPasswordEmpty = TextUtils.isEmpty(newPasswordText);
                    boolean isConfirmPasswordEmpty = TextUtils.isEmpty(confirmPasswordText);

                    if (isOldPasswordEmpty || isNewPasswordEmpty || isConfirmPasswordEmpty) {
                        DialogUtils.show(getActivity(), getString(R.string.dlg_not_all_fields_entered));
                        yesButton.setClickable(false);
                        return false;
                    }

                    if (!newPasswordText.equals(confirmPasswordText)) {
                        DialogUtils.show(getActivity(), getString(R.string.dlg_passwords_not_equal));
                        yesButton.setClickable(false);
                        return false;
                    }
                    yesButton.setClickable(true);
                    return false;
                }
            });
        }
    }
}
