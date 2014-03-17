package com.quickblox.qmunicate.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.command.Command;
import com.quickblox.qmunicate.qb.QBSocialLoginCommand;
import com.quickblox.qmunicate.qb.QBLoginCommand;
import com.quickblox.qmunicate.qb.QBResetPasswordCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.signup.SignUpActivity;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText email;
    private EditText password;
    private CheckBox rememberMe;

    private FacebookHelper facebookHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        useDoubleBackPressed = true;

        email = _findViewById(R.id.email);
        password = _findViewById(R.id.password);
        rememberMe = _findViewById(R.id.rememberMe);

        boolean isRememberMe = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, false);
        rememberMe.setChecked(isRememberMe);

        addAction(QBServiceConsts.LOGIN_SUCESS_ACTION, new LoginSuccessAction());
        addAction(QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION, new ResetPasswordSuccessAction());
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, new FailAction(this));
        addAction(QBServiceConsts.RESET_PASSWORD_FAIL_ACTION, new FailAction(this));
        updateBroadcastActionList();

        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_register:
                SignUpActivity.start(LoginActivity.this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        facebookHelper.onSaveInstanceState(outState);
    }

    public void loginOnClickListener(View view) {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);

        if (isEmailEntered && isPasswordEntered) {
            login(userEmail, userPassword);
        } else {
            DialogUtils.show(LoginActivity.this, getString(R.string.dlg_not_all_fields_entered));
        }
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        showProgress();
        QBLoginCommand.start(this, user);
    }

    public void loginFacebookOnClickListener(View view) {
        facebookHelper.loginWithFacebook();
    }

    public void forgotPasswordOnClickListener(View view) {
        String userEmail = email.getText().toString();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);

        if (isEmailEntered) {
            showProgress();
            QBResetPasswordCommand.start(this, userEmail);
        } else {
            DialogUtils.show(this, getString(R.string.dlg_empty_email));
        }
    }

    private void saveRememberMe(boolean value) {
        App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_REMEMBER_ME, value);
    }

    private void saveUserCredentials(QBUser user) {
        PrefsHelper helper = App.getInstance().getPrefsHelper();
        helper.savePref(PrefsHelper.PREF_USER_EMAIL, user.getEmail());
        helper.savePref(PrefsHelper.PREF_USER_PASSWORD, user.getPassword());
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                showProgress();
                QBSocialLoginCommand.start(LoginActivity.this, QBProvider.FACEBOOK, session.getAccessToken(), null);
            }
        }
    }

    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            App.getInstance().setUser(user);
            if (rememberMe.isChecked()) {
                saveRememberMe(true);
                saveUserCredentials(user);
            }
            hideProgress();
            MainActivity.start(LoginActivity.this);
            finish();
        }
    }

    private class ResetPasswordSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            String emailText = bundle.getString(QBServiceConsts.EXTRA_EMAIL);
            DialogUtils.show(LoginActivity.this, getString(R.string.dlg_check_email, emailText));
        }
    }
}
