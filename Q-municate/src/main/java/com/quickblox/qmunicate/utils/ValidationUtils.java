package com.quickblox.qmunicate.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;

public class ValidationUtils extends SimpleTextWatcher {

    private Context context;
    private EditText fullnameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText[] fieldsArray;

    public ValidationUtils(Context context, EditText emailEditText, EditText passwordEditText) {
        this.context = context;
        this.emailEditText = emailEditText;
        this.passwordEditText = passwordEditText;
        fieldsArray = new EditText[]{emailEditText, passwordEditText};
        initListeners();
    }

    public ValidationUtils(Context context, EditText fullnameEditText, EditText emailEditText,
                           EditText passwordEditText) {
        this.context = context;
        this.fullnameEditText = fullnameEditText;
        this.emailEditText = emailEditText;
        this.passwordEditText = passwordEditText;
        fieldsArray = new EditText[]{fullnameEditText, emailEditText, passwordEditText};
        initListeners();
    }

    private void initListeners() {
        for (int i = 0; i < fieldsArray.length; i++) {
            fieldsArray[i].addTextChangedListener(this);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        super.onTextChanged(s, start, before, count);
        setError(null);
    }

    public boolean isValidUserDate(String fullNameText, String emailText, String passwordText) {
        boolean isFullNameEntered = !TextUtils.isEmpty(fullNameText);
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);
        boolean isPasswordEntered = !TextUtils.isEmpty(passwordText);

        if (isFullNameEntered && isEmailEntered && isPasswordEntered) {
            return true;
        } else if (!isFullNameEntered && !isEmailEntered && !isPasswordEntered) {
            setError(context.getString(R.string.dlg_not_all_fields_entered));
        } else {
            setErrors(isFullNameEntered, isEmailEntered, isPasswordEntered);
        }

        return false;
    }

    public boolean isValidUserDate(String emailText, String passwordText) {
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);
        boolean isPasswordEntered = !TextUtils.isEmpty(passwordText);

        if (isEmailEntered && isPasswordEntered) {
            return true;
        } else if (!isEmailEntered && !isPasswordEntered) {
            setError(context.getString(R.string.dlg_not_all_fields_entered));
        } else {
            setErrors(isEmailEntered, isPasswordEntered);
        }

        return false;
    }

    public void setError(String error) {
        for (int i = 0; i < fieldsArray.length; i++) {
            fieldsArray[i].setError(error);
        }
    }

    private void setErrors(boolean isFullNameEntered, boolean isEmailEntered, boolean isPasswordEntered) {
        fullnameEditText.setError(isFullNameEntered ? null : context.getString(R.string.dlg_not_fullname_field_entered));
        setErrors(isEmailEntered, isPasswordEntered);
    }

    private void setErrors(boolean isEmailEntered, boolean isPasswordEntered) {
        emailEditText.setError(isEmailEntered ? null : context.getString(R.string.dlg_not_email_field_entered));
        passwordEditText.setError(isPasswordEntered ? null : context.getString(R.string.dlg_not_password_field_entered));
    }
}