package com.quickblox.q_municate.ui.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.forgotpassword.ForgotPasswordActivity;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_db.managers.DatabaseManager;

public class LoginActivity extends BaseAuthActivity {

    private CheckBox rememberMeCheckBox;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initUI();

        rememberMeCheckBox.setChecked(true);

        validationUtils = new ValidationUtils(LoginActivity.this,
                new EditText[]{emailEditText, passwordEditText}, new String[]{resources.getString(
                R.string.dlg_not_email_field_entered), resources.getString(
                R.string.dlg_not_password_field_entered)});
    }

    @Override
    public void onBackPressed() {
        LandingActivity.start(this);
        finish();
    }

    public void loginOnClickListener(View view) {
        String userEmail = emailEditText.getText().toString();
        String userPassword = passwordEditText.getText().toString();

        if (validationUtils.isValidUserDate(userEmail, userPassword)) {
            showProgress();

            boolean ownerUser = DatabaseManager.getInstance().getUserManager().isUserOwner(userEmail);
            if (!ownerUser) {
                ChatDatabaseManager.clearAllCache(this);
            }

            initCheckedRememberMe();
            login(userEmail, userPassword);
        }
    }

    public void forgotPasswordOnClickListener(View view) {
        startChangePasswordActivity();
    }

    private void startChangePasswordActivity() {
        ForgotPasswordActivity.start(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        emailEditText = _findViewById(R.id.email_textview);
        passwordEditText = _findViewById(R.id.password_edittext);
        rememberMeCheckBox = _findViewById(R.id.remember_me_checkbox);
    }

    private void initCheckedRememberMe() {
        if (rememberMeCheckBox.isChecked()) {
            checkedRememberMe = true;
        }
    }
}