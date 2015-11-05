package com.quickblox.q_municate.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.simple.SimpleTextWatcher;

// need to refactor, bad logic
@Deprecated
public class ValidationUtils_OLD extends SimpleTextWatcher {

    private Context context;
    private EditText[] fieldsArray;
    private String[] fieldsErrorArray;

    public ValidationUtils_OLD(Context context, EditText[] fieldsArray, String[] fieldsErrorArray) {
        this.context = context;
        this.fieldsArray = fieldsArray;
        this.fieldsErrorArray = fieldsErrorArray;
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
            setErrors(new boolean[]{isFullNameEntered, isEmailEntered, isPasswordEntered});
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
            setErrors(new boolean[]{isEmailEntered, isPasswordEntered});
        }

        return false;
    }

    public boolean isValidForgotPasswordData(String emailText) {
        boolean isEmailEntered = !TextUtils.isEmpty(emailText);

        if (isEmailEntered) {
            return true;
        } else {
            setErrors(new boolean[]{isEmailEntered});
        }

        return false;
    }

    public void setError(String error) {
        if (fieldsArray != null) {
            for (int i = 0; i < fieldsArray.length; i++) {
                fieldsArray[i].setError(error);
            }
        }
    }

    private void setErrors(boolean[] isFieldsEnteredArray) {
        for (int i = 0; i < fieldsArray.length; i++) {
            fieldsArray[i].setError(isFieldsEnteredArray[i] ? null : fieldsErrorArray[i]);
        }
    }
}