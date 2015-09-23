package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.forgotpassword.ForgotPasswordActivity;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_db.managers.DataManager;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class LoginActivity extends BaseAuthActivity {

    @Bind(R.id.email_edittext)
    EditText emailEditText;

    @Bind(R.id.password_edittext)
    EditText passwordEditText;

    @Bind(R.id.remember_me_switch)
    SwitchCompat rememberMeSwitch;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        activateButterKnife();

        initActionBar();
        initFields(savedInstanceState);
    }

    @Override
    public void initActionBar() {
        super.initActionBar();
        setActionBarUpButtonEnabled(true);
    }

    private void initFields(Bundle bundle) {
        validationUtils = new ValidationUtils(LoginActivity.this,
                new EditText[]{emailEditText, passwordEditText},
                new String[]{
                getString(R.string.dlg_not_email_field_entered),
                getString(R.string.dlg_not_password_field_entered)});
        rememberMeSwitch.setChecked(true);
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
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_done:
                login();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        LandingActivity.start(this);
        finish();
    }

    @OnCheckedChanged(R.id.remember_me_switch)
    public void rememberMeCheckedChanged(boolean checked) {
        appSharedHelper.saveSavedRememberMe(checked);
    }

    @OnClick(R.id.forgot_password_textview)
    public void forgotPassword(View view) {
        ForgotPasswordActivity.start(this);
    }

    private void login() {
        loginType = LoginType.EMAIL;

        String userEmail = emailEditText.getText().toString();
        String userPassword = passwordEditText.getText().toString();

        if (validationUtils.isValidUserDate(userEmail, userPassword)) {
            showProgress();

            boolean ownerUser = DataManager.getInstance().getUserDataManager().isUserOwner(userEmail);
            if (!ownerUser) {
                DataManager.getInstance().clearAllTables();
            }

            login(userEmail, userPassword);
        }
    }
}