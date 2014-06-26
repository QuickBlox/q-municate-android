package com.quickblox.qmunicate.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.quickblox.qmunicate.model.AppSession;
import com.quickblox.qmunicate.model.LoginType;
import com.quickblox.qmunicate.qb.commands.QBLoginCommand;
import com.quickblox.qmunicate.qb.commands.QBLoginRestWithSocialCommand;
import com.quickblox.qmunicate.qb.commands.QBResetPasswordCommand;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.landing.LandingActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.ui.uihelper.SimpleTextWatcher;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String STARTED_LOGIN_TYPE = "started_login_type";

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private FacebookHelper facebookHelper;
    private LoginType startedLoginType = LoginType.EMAIL;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            startedLoginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }
        initUI();
        boolean isRememberMe = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, true);
        rememberMeCheckBox.setChecked(isRememberMe);
        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        initListeners();
        addActions();
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
    public void onBackPressed() {
        LandingActivity.start(this);
        finish();
    }


    public void loginOnClickListener(View view) {
        String userEmail = emailEditText.getText().toString();
        String userPassword = passwordEditText.getText().toString();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);
        boolean isPasswordEntered = !TextUtils.isEmpty(userPassword);

        if (isEmailEntered && isPasswordEntered) {
            login(userEmail, userPassword);
        } else {
            DialogUtils.showLong(LoginActivity.this, getString(R.string.dlg_not_all_fields_entered));
        }
    }

    public void loginFacebookOnClickListener(View view) {
        startedLoginType = LoginType.FACEBOOK;
        facebookHelper.loginWithFacebook();
    }

    public void forgotPasswordOnClickListener(View view) {
        String userEmail = emailEditText.getText().toString();

        boolean isEmailEntered = !TextUtils.isEmpty(userEmail);

        if (isEmailEntered) {
            showProgress();
            QBResetPasswordCommand.start(this, userEmail);
        } else {
            DialogUtils.showLong(this, getString(R.string.dlg_empty_email));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STARTED_LOGIN_TYPE, startedLoginType);
        facebookHelper.onSaveInstanceState(outState);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void initUI() {
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        emailEditText = _findViewById(R.id.email_textview);
        passwordEditText = _findViewById(R.id.password_edittext);
        rememberMeCheckBox = _findViewById(R.id.remember_me_checkbox);
    }

    private void initListeners() {
        emailEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                super.onTextChanged(charSequence, start, before, count);
                emailEditText.setError(null);
            }
        });
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, new LoginSuccessAction());
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, new LoginFailAction());
        addAction(QBServiceConsts.RESET_PASSWORD_SUCCESS_ACTION, new ResetPasswordSuccessAction());
        addAction(QBServiceConsts.RESET_PASSWORD_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        showProgress();
        QBLoginCommand.start(this, user);
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
                // TODO SF must be
                // QBUser user = FacebookHelper.getCurrentFacebookUser(session);
                QBLoginRestWithSocialCommand.start(LoginActivity.this, QBProvider.FACEBOOK,
                        session.getAccessToken(), null);
            }
        }
    }

    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            AppSession.startSession(startedLoginType, user);
            if (rememberMeCheckBox.isChecked()) {
                saveRememberMe(true);
                saveUserCredentials(user);
            }
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
            MainActivity.start(LoginActivity.this);
            finish();
        }
    }


    private class LoginFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            emailEditText.setError(getResources().getString(R.string.lgn_error));
        }
    }

    private class ResetPasswordSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            String emailText = bundle.getString(QBServiceConsts.EXTRA_EMAIL);
            DialogUtils.showLong(LoginActivity.this, getString(R.string.dlg_check_email, emailText));
        }
    }
}
