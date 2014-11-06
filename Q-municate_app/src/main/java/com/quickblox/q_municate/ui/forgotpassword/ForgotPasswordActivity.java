package com.quickblox.q_municate.ui.forgotpassword;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBResetPasswordCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate.utils.KeyboardUtils;
import com.quickblox.q_municate.utils.ValidationUtils;

public class ForgotPasswordActivity extends BaseActivity {

    private EditText emailEditText;
    private ValidationUtils validationUtils;
    private Resources resources;

    public static void start(Context context) {
        Intent intent = new Intent(context, ForgotPasswordActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        resources = getResources();

        initUI();

        addActions();
    }

    public void forgotPasswordOnClickListener(View view) {
        KeyboardUtils.hideKeyboard(this);
        String emailText = emailEditText.getText().toString();
        if (validationUtils.isValidForgotPasswordData(emailText)) {
            showProgress();
            QBResetPasswordCommand.start(this, emailText);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParent();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        emailEditText = _findViewById(R.id.email_edittext);
        validationUtils = new ValidationUtils(this, new EditText[]{emailEditText},
                new String[]{resources.getString(R.string.fpw_not_email_field_entered)});
    }

    private void addActions() {
        addAction(QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION, new ResetPasswordSuccessAction());
        addAction(QBServiceConsts.RESET_PASSWORD_FAIL_ACTION, new ResetPasswordFailAction());
        updateBroadcastActionList();
    }

    private class ResetPasswordSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            String emailText = bundle.getString(QBServiceConsts.EXTRA_EMAIL);
            DialogUtils.showLong(ForgotPasswordActivity.this, getString(R.string.fpw_email_was_sent,
                    emailText));
        }
    }

    private class ResetPasswordFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            hideProgress();
            emailEditText.setError(exception.getMessage());
        }
    }
}