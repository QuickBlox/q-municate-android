package com.quickblox.qmunicate.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.module.auth.model.QBProvider;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.qb.QBLoginTask;
import com.quickblox.qmunicate.qb.QBResetPasswordTask;
import com.quickblox.qmunicate.qb.QBSocialLoginTask;
import com.quickblox.qmunicate.ui.base.FacebookActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.registration.RegistrationActivity;
import com.quickblox.qmunicate.ui.utils.DialogUtils;
import com.quickblox.qmunicate.ui.utils.PrefsHelper;

public class LoginActivity extends FacebookActivity implements QBLoginTask.Callback {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String DEFAULT_EMAIL = "shaforenko.igor@injoit.com";
    private static final String DEFAULT_PASSWORD = "qweqweqwe";

    private Button loginButton;
    private View loginFacebokButton;
    private EditText email;
    private EditText password;
    private TextView forgotPassword;
    private CheckBox rememberMe;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        useDoubleBackPressed = true;
        facebookStatusCallback = new FacebookSessionStatusCallback();

        email = _findViewById(R.id.email);
        password = _findViewById(R.id.password);
        loginButton = _findViewById(R.id.loginButton);
        loginFacebokButton = _findViewById(R.id.connectFacebookButton);
        forgotPassword = _findViewById(R.id.forgotPassword);
        rememberMe = _findViewById(R.id.rememberMe);

        email.setText(DEFAULT_EMAIL);
        password.setText(DEFAULT_PASSWORD);

        initListeners();
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
                RegistrationActivity.startActivity(LoginActivity.this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSuccess(Bundle bundle) {
        QBUser user = (QBUser) bundle.getSerializable(QBLoginTask.PARAM_QBUSER);
        if (rememberMe.isChecked()) {
            saveUserCredentials(user);
        }
        MainActivity.startActivity(LoginActivity.this);
        finish();
    }

    private void initListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        loginFacebokButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithFacebook();
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void login() {
        String userEmail = email.getText().toString();
        String userPassword = password.getText().toString();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);

        if (isEmailEntered && isPasswordEntered) {
            saveRememberMe(rememberMe.isChecked());
            final QBUser user = new QBUser(null, userPassword, userEmail);
            new QBLoginTask(LoginActivity.this).execute(user, this);
        } else {
            DialogUtils.show(LoginActivity.this, getString(R.string.dlg_not_all_fields_entered));
        }
    }

    private void resetPassword() {
        String userEmail = email.getText().toString();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);

        if (isEmailEntered) {
            new QBResetPasswordTask(this).execute(userEmail);
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
                new QBSocialLoginTask(LoginActivity.this).execute(QBProvider.FACEBOOK, session.getAccessToken(), null);
            }
        }
    }
}
