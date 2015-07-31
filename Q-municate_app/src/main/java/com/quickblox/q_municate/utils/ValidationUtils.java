package com.quickblox.q_municate.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.EditText;

import com.quickblox.users.model.QBUser;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate.ui.uihelper.SimpleTextWatcher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationUtils extends SimpleTextWatcher {

    private final int UNAUTHORIZED_ERROR_CODE = 401;
    private final int FULLNAME_IS_TOO_SHORT_ERROR_CODE = 422;
    private final int EMAIL_SHOULD_LOOK_LIKE_AN_EMAIL_ADDRESS_ERROR_CODE = 422;
    private final int PASSWORD_IS_TOO_SHORT_ERROR_CODE = 422;
    private final int PASSWORD_SHOULD_CONTAIN_ALPHANUMERIC_AND_PUNCTUATION_CHARACTERS_ERROR_CODE = 422;

    private Context context;
    private EditText[] fieldsArray;
    private String[] fieldsErrorArray;

    public ValidationUtils(Context context, EditText[] fieldsArray, String[] fieldsErrorArray) {
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
        boolean isFullNameValid = isValidName(fieldsArray[0], fullNameText);
        boolean isEmailValid = isValidEmail(fieldsArray[1], emailText);
        boolean isPasswordValid = isValidPassword(fieldsArray[2], passwordText);

        if (isFullNameValid && isEmailValid && isPasswordValid) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidUserDate(String emailText, String passwordText) {
        boolean isEmailValid = isValidEmail(fieldsArray[0], emailText);
        boolean isPasswordValid = isValidPassword(fieldsArray[1], passwordText);

        if (isEmailValid && isPasswordValid) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isValidName(EditText fullNameEditText, String fullNameText) {

        int counterSymbols = 0;
        char[] fullNameTextToCharArray = fullNameText.toCharArray();
        char[] symbols = {'<', '>', ';'};

        for (int i = 0; i < fullNameTextToCharArray.length; i++) {
            for (int j = 0; j < symbols.length; j++) {
                if (fullNameTextToCharArray[i] == symbols[j]) {
                    counterSymbols++;
                    break;
                }
            }
        }

        if (!fullNameText.isEmpty()) {
            if (fullNameText.length() < 3) {
                fullNameEditText.setError(context.getString(R.string.error_name_must_be_more_than_2_characters_from_app));
                return false;
            } else if (fullNameText.length() > 50){
                fullNameEditText.setError(context.getString(R.string.error_is_too_long_maximum_is_50_characters_from_app));
                return false;
            } else if (counterSymbols != 0) {
                fullNameEditText.setError(context.getString(R.string.error_name_must_do_not_contain_special_characters_from_app));
                return false;
            } else {
                return true;
            }
        } else {
            fullNameEditText.setError(context.getString(R.string.dlg_not_fullname_field_entered));
            return false;
        }
    }

    public boolean isValidEmail(EditText emailEditText, String emailText){
        boolean isMailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches();

        if (!isMailValid){
            emailEditText.setError(context.getString(R.string.error_please_enter_a_valid_email_from_app));
        }

        return isMailValid;
    }

    public boolean isValidPassword(EditText passwordEditText, String passwordText) {
        boolean onlyCorrectSymbols;
        Pattern p = Pattern.compile("^[-A-Za-z0-9\\]\\[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\.\\/\\:\\;\\<\\=\\>\\?\\@\\\\\\^\\_\\`\\{\\|\\}\\~]+");
        Matcher m = p.matcher(passwordText);
        onlyCorrectSymbols = m.matches();

        if (!passwordText.isEmpty()) {
            if (passwordEditText.length() < 8) {
                passwordEditText.setError(context.getString(R.string.error_password_must_be_more_than_7_characters_from_app));
                return false;
            } else if (passwordEditText.length() > 40){
                    passwordEditText.setError(context.getString(R.string.error_password_must_do_not_be_more_than_40_characters_from_app));
                    return false;
            } else if (!onlyCorrectSymbols) {
                passwordEditText.setError(context.getString(R.string.error_password_must_do_not_contain_non_Latin_characters_and_spaces_from_app));
                return false;
            } else {
                return true;
            }
        } else {
            passwordEditText.setError(context.getString(R.string.dlg_not_password_field_entered));
            return false;
        }
    }

    public boolean isValidChangePasswordData(String oldPasswordText, String newPasswordText) {
        QBUser user = AppSession.getSession().getUser();

        boolean isOldPasswordEntered = !TextUtils.isEmpty(oldPasswordText);
        boolean isNewPasswordValid = isValidPassword(fieldsArray[1], newPasswordText);

        if (isOldPasswordEntered && isNewPasswordValid) {
            if (!user.getPassword().equals(oldPasswordText)) {
                setError(0, context.getString(R.string.dlg_old_password_wrong));
                return false;
            } else {
                return true;
            }
        } else if (!isOldPasswordEntered) {
            setError(0, fieldsErrorArray[0]);
            return false;
        } else {
            return false;
        }
    }

    public boolean isValidForgotPasswordData(String emailText) {
        boolean isEmailValid = isValidEmail(fieldsArray[0], emailText);

        if (isEmailValid) {
            return true;
        } else {
            return false;
        }
    }

    public void setError(String error) {
        for (int i = 0; i < fieldsArray.length; i++) {
            fieldsArray[i].setError(error);
        }
    }

    private void setError(int index, String error) {
        fieldsArray[index].setError(error);
    }

    private void setErrors(boolean[] isFieldsEnteredArray) {
        for (int i = 0; i < fieldsArray.length; i++) {
            fieldsArray[i].setError(isFieldsEnteredArray[i] ? null : fieldsErrorArray[i]);
        }
    }

    // TODO SF temp method
    public String getErrorMessageByCode(int errorCode) {
        String errorMessage = "ERROR";
        switch (errorCode) {
            case 1:
                errorMessage = "error 1";
                break;
        }
        return errorMessage;
    }
}