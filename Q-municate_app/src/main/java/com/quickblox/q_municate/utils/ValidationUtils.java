package com.quickblox.q_municate.utils;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;

public class ValidationUtils {

    private Context context;
    private QBUser qbUser;

    public ValidationUtils(Context context) {
        this.context = context;
        this.qbUser = AppSession.getSession().getUser();
    }

    public boolean isValidChangePasswordData(TextInputLayout oldPasswordTextInputLayout,
            TextInputLayout newPasswordTextInputLayout, String oldPasswordText, String newPasswordText) {
        boolean isOldPasswordEntered = !TextUtils.isEmpty(oldPasswordText);
        boolean isNewPasswordEntered = !TextUtils.isEmpty(newPasswordText);

        if (isOldPasswordEntered && isNewPasswordEntered) {
            if (!qbUser.getPassword().equals(oldPasswordText)) {
                oldPasswordTextInputLayout.setError(context.getString(R.string.dlg_old_password_wrong));
            } else {
                return true;
            }
        } else if (!isOldPasswordEntered && !isNewPasswordEntered) {
            oldPasswordTextInputLayout.setError(context.getString(R.string.dlg_not_all_fields_entered));
            newPasswordTextInputLayout.setError(context.getString(R.string.dlg_not_all_fields_entered));
        } else {
            if (!isOldPasswordEntered) {
                oldPasswordTextInputLayout
                        .setError(context.getString(R.string.change_password_not_old_password_field_entered));
            }
            if (!isNewPasswordEntered) {
                newPasswordTextInputLayout
                        .setError(context.getString(R.string.change_password_not_new_password_field_entered));
            }
        }

        return false;
    }
}