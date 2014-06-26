package com.quickblox.qmunicate.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
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
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.forgotpassword.ForgotPasswordActivity;
import com.quickblox.qmunicate.ui.landing.LandingActivity;
import com.quickblox.qmunicate.ui.main.MainActivity;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;
import com.quickblox.qmunicate.utils.ValidationUtils;

public class LoginActivity extends BaseActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final String STARTED_LOGIN_TYPE = "started_login_type";

    private EditText emailEditText;
    private EditText passwordEditText;
    private CheckBox rememberMeCheckBox;
    private FacebookHelper facebookHelper;
    private LoginType startedLoginType = LoginType.EMAIL;
    private ValidationUtils validationUtils;
    private Resources resources;

    public static void start(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        resources = getResources();
        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            startedLoginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }
        canPerformLogout.set(false);
        initUI();
        boolean isRememberMe = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_REMEMBER_ME, true);
        rememberMeCheckBox.setChecked(isRememberMe);
        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

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

        if (validationUtils.isValidUserDate(userEmail, userPassword)) {
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
            login(userEmail, userPassword);
        }
    }

    public void loginFacebookOnClickListener(View view) {
        startedLoginType = LoginType.FACEBOOK;
        facebookHelper.loginWithFacebook();
    }

    public void forgotPasswordOnClickListener(View view) {
        startChangePasswordActivity();
    }

    private void startChangePasswordActivity() {
        ForgotPasswordActivity.start(this);
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
        validationUtils = new ValidationUtils(LoginActivity.this, new EditText[] {emailEditText, passwordEditText},
                new String[]{resources.getString(R.string.dlg_not_email_field_entered), resources.getString(R.string.dlg_not_password_field_entered)});
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, new LoginSuccessAction());
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, new LoginFailAction());
        updateBroadcastActionList();
    }

    private void login(String userEmail, String userPassword) {
        QBUser user = new QBUser(null, userPassword, userEmail);
        showProgress();
        QBLoginCommand.start(this, user);
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                showProgress();
                QBLoginRestWithSocialCommand.start(LoginActivity.this, QBProvider.FACEBOOK,
                        session.getAccessToken(), null);
            }
        }
    }

    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            if (rememberMeCheckBox.isChecked()) {
                AppSession.saveRememberMe(true);
                AppSession.saveUserCredentials(user);
            }
            MainActivity.start(LoginActivity.this);
            finish();
        }
    }

    private class LoginFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            hideProgress();
            validationUtils.setError(getResources().getString(R.string.lgn_error));
        }
    }
}