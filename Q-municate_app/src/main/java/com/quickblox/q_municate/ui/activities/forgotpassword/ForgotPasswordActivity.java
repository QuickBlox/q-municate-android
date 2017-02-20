package com.quickblox.q_municate.ui.activities.forgotpassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.result.HttpStatus;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.utils.helpers.ServiceManager;
import com.quickblox.q_municate.utils.ToastUtils;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.utils.KeyboardUtils;

import butterknife.Bind;
import butterknife.OnTextChanged;
import rx.Subscriber;

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
        final String emailText = emailEditText.getText().toString();
        if (new ValidationUtils(this).isForgotPasswordDataValid(emailTextInputLayout, emailText)) {
            showProgress();
            ServiceManager.getInstance().resetPassword(emailText).subscribe(new Subscriber<Void>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {
                    if (e != null) {
                        emailEditText.setError(e.getMessage());
                    }

                    hideProgress();
                }

                @Override
                public void onNext(Void aVoid) {
                    hideProgress();
                    ToastUtils.longToast(getString(R.string.forgot_password_massage_email_was_sent, emailText));
                }
            });
        }
    }
}