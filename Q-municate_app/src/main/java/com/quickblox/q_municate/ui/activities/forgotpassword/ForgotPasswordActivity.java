package com.quickblox.q_municate.ui.activities.forgotpassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.qb.commands.QBResetPasswordCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.KeyboardUtils;

import butterknife.Bind;
import butterknife.OnTextChanged;

public class ForgotPasswordActivity extends BaseActivity {

    @Bind(R.id.email_textinputlayout)
    TextInputLayout emailTextInputLayout;

    @Bind(R.id.email_edittext)
    EditText emailEditText;

    public static void start(Context context) {
        Intent intent = new Intent(context, ForgotPasswordActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_forgot_password;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        setUpActionBarWithUpButton();
        addActions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.done_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                if (checkNetworkAvailableWithError()) {
                    forgotPassword();
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeActions();
    }

    @OnTextChanged(R.id.email_edittext)
    void onTextChangedEmail(CharSequence text) {
        emailTextInputLayout.setError(null);
    }

    private void initFields() {
        title = getString(R.string.forgot_password_title);
    }

    private void forgotPassword() {
        KeyboardUtils.hideKeyboard(this);
        String emailText = emailEditText.getText().toString();
        if (new ValidationUtils(this).isForgotPasswordDataValid(emailTextInputLayout, emailText)) {
            showProgress();
            QBResetPasswordCommand.start(this, emailText);
        }
    }

    private void addActions() {
        addAction(QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION, new ResetPasswordSuccessAction());
        addAction(QBServiceConsts.RESET_PASSWORD_FAIL_ACTION, new ResetPasswordFailAction());

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION);
        removeAction(QBServiceConsts.RESET_PASSWORD_FAIL_ACTION);

        updateBroadcastActionList();
    }

    private class ResetPasswordSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            String emailText = bundle.getString(QBServiceConsts.EXTRA_EMAIL);
            ToastUtils.longToast(getString(R.string.forgot_password_massage_email_was_sent, emailText));
        }
    }

    private class ResetPasswordFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
            if (exception != null) {
                emailEditText.setError(exception.getMessage());
            }

            hideProgress();
        }
    }
}