package com.quickblox.q_municate.utils;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.users.model.QBUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final int FULL_NAME_MIN_LENGTH = 3;
    private static final int FULL_NAME_MAX_LENGTH = 50;
    private static final int PASSWORD_MIN_LENGTH = 8;

    private Context context;
    private QBUser qbUser;

    public ValidationUtils(Context context) {
        this.context = context;
        this.qbUser = AppSession.getSession().getUser();
    }

    public boolean isLoginDataValid(TextInputLayout emailTextInputLayout,
            TextInputLayout passwordTextInputLayout, String email, String password) {
        boolean isEmailEntered = !TextUtils.isEmpty(email.trim());
        boolean isPasswordEntered = !TextUtils.isEmpty(password.trim());

        if (isEmailEntered && isPasswordEntered) {
            boolean valid = true;
            if (!isEmailValid(email)) {
                valid = false;
                emailTextInputLayout.setError(context.getString(R.string.auth_email_field_is_incorrect));
            }
            if (password.length() < PASSWORD_MIN_LENGTH) {
                valid = false;
                passwordTextInputLayout.setError(context.getString(R.string.auth_password_field_is_too_short));
            }
            return valid;
        } else if (!isEmailEntered && !isPasswordEntered) {
            emailTextInputLayout.setError(context.getString(R.string.auth_not_all_fields_entered));
            passwordTextInputLayout.setError(context.getString(R.string.auth_not_all_fields_entered));
        } else {
            if (!isEmailEntered) {
                emailTextInputLayout.setError(context.getString(R.string.auth_email_field_not_entered));
            }
            if (!isPasswordEntered) {
                passwordTextInputLayout.setError(context.getString(R.string.auth_password_field_not_entered));
            }
        }

        return false;
    }

    public boolean isSignUpDataValid(TextInputLayout fullNameTextInputLayout,
            TextInputLayout emailTextInputLayout, TextInputLayout passwordTextInputLayout, String fullName,
            String email, String password) {
        boolean isFullNameEntered = !TextUtils.isEmpty(fullName.trim());
        boolean isEmailEntered = !TextUtils.isEmpty(email.trim());
        boolean isPasswordEntered = !TextUtils.isEmpty(password.trim());

        if (isFullNameEntered && isEmailEntered && isPasswordEntered) {
            boolean valid = true;
            if (fullName.length() < FULL_NAME_MIN_LENGTH) {
                valid = false;
                fullNameTextInputLayout.setError(context.getString(R.string.auth_full_name_field_is_too_short));
            } else if (fullName.length() > FULL_NAME_MAX_LENGTH) {
                valid = false;
                fullNameTextInputLayout.setError(context.getString(R.string.auth_full_name_field_is_too_long));
            }
            if (!isEmailValid(email)) {
                valid = false;
                emailTextInputLayout.setError(context.getString(R.string.auth_email_field_is_incorrect));
            }
            if (password.length() < PASSWORD_MIN_LENGTH) {
                valid = false;
                passwordTextInputLayout.setError(context.getString(R.string.auth_password_field_is_too_short));
            }
            return valid;
        } else if (!isFullNameEntered && !isEmailEntered && !isPasswordEntered) {
            fullNameTextInputLayout.setError(context.getString(R.string.auth_not_all_fields_entered));
            emailTextInputLayout.setError(context.getString(R.string.auth_not_all_fields_entered));
            passwordTextInputLayout.setError(context.getString(R.string.auth_not_all_fields_entered));
        } else {
            if (!isFullNameEntered) {
                fullNameTextInputLayout
                        .setError(context.getString(R.string.auth_full_name_field_not_entered));
            }
            if (!isEmailEntered) {
                emailTextInputLayout.setError(context.getString(R.string.auth_email_field_not_entered));
            }
            if (!isPasswordEntered) {
                passwordTextInputLayout.setError(context.getString(R.string.auth_password_field_not_entered));
            }
        }

        return false;
    }

    public boolean isChangePasswordDataValid(TextInputLayout oldPasswordTextInputLayout,
            TextInputLayout newPasswordTextInputLayout, String oldPassword, String newPassword) {
        boolean isOldPasswordEntered = !TextUtils.isEmpty(oldPassword.trim());
        boolean isNewPasswordEntered = !TextUtils.isEmpty(newPassword.trim());

        if (isOldPasswordEntered && isNewPasswordEntered) {
            if (!qbUser.getPassword().equals(oldPassword)) {
                oldPasswordTextInputLayout.setError(context.getString(R.string.change_password_old_password_wrong));
            } else {
                return true;
            }
        } else if (!isOldPasswordEntered && !isNewPasswordEntered) {
            oldPasswordTextInputLayout.setError(context.getString(R.string.change_password_all_fields_not_entered));
            newPasswordTextInputLayout.setError(context.getString(R.string.change_password_all_fields_not_entered));
        } else {
            if (!isOldPasswordEntered) {
                oldPasswordTextInputLayout
                        .setError(context.getString(R.string.change_password_old_password_not_entered));
            }
            if (!isNewPasswordEntered) {
                newPasswordTextInputLayout
                        .setError(context.getString(R.string.change_password_new_password_not_entered));
            }
        }

        return false;
    }

    public boolean isFullNameValid(TextInputLayout fullNameTextInputLayout, String oldFullName,
            String newFullName) {
        boolean fullNameEntered = !TextUtils.isEmpty(newFullName.trim());

        if (fullNameEntered) {
            if (newFullName.equals(oldFullName)) {
                fullNameTextInputLayout.setError(context.getString(R.string.profile_full_name_not_changed));
            } else {
                return true;
            }
        } else {
            fullNameTextInputLayout.setError(context.getString(R.string.profile_full_name_not_entered));
        }

        return false;
    }

    public boolean isForgotPasswordDataValid(TextInputLayout emailTextInputLayout, String email) {
        boolean isEmailEntered = !TextUtils.isEmpty(email.trim());

        if (isEmailEntered) {
            if (!isEmailValid(email)) {
                emailTextInputLayout.setError(context.getString(R.string.forgot_password_email_field_is_incorrect));
            } else {
                return true;
            }
        } else {
            emailTextInputLayout.setError(context.getString(R.string.forgot_password_email_field_not_entered));
        }

        return false;
    }

    private boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}