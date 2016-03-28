package com.quickblox.q_municate.ui.activities.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.activities.base.BaseActivity;
import com.quickblox.q_municate.ui.activities.main.MainActivity;
import com.quickblox.q_municate.ui.fragments.dialogs.UserAgreementDialogFragment;
import com.quickblox.q_municate.utils.helpers.FlurryAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.GoogleAnalyticsHelper;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBUpdateUserCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.rest.QBSocialLoginCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.users.model.QBUser;

import butterknife.Bind;
import butterknife.OnTextChanged;

public abstract class BaseAuthActivity extends BaseActivity {

    private static String TAG = BaseAuthActivity.class.getSimpleName();

    protected static final String STARTED_LOGIN_TYPE = "started_login_type";

    @Nullable
    @Bind(R.id.email_textinputlayout)
    protected TextInputLayout emailTextInputLayout;

    @Nullable
    @Bind(R.id.email_edittext)
    protected EditText emailEditText;

    @Nullable
    @Bind(R.id.password_textinputlayout)
    protected TextInputLayout passwordTextInputLayout;

    @Nullable
    @Bind(R.id.password_edittext)
    protected EditText passwordEditText;

    protected FacebookHelper facebookHelper;
    protected LoginType loginType = LoginType.EMAIL;
    protected Resources resources;

    protected LoginSuccessAction loginSuccessAction;
    protected SocialLoginSuccessAction socialLoginSuccessAction;
    protected FailAction failAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, BaseAuthActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        facebookHelper.onActivityStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addActions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
    }

    @Override
    public void onStop() {
        super.onStop();
        facebookHelper.onActivityStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STARTED_LOGIN_TYPE, loginType);
        facebookHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @OnTextChanged(R.id.email_edittext)
    void onTextChangedEmail(CharSequence text) {
        emailTextInputLayout.setError(null);
    }

    @Nullable
    @OnTextChanged(R.id.password_edittext)
    void onTextChangedPassword(CharSequence text) {
        passwordTextInputLayout.setError(null);
    }

    private void initFields(Bundle savedInstanceState) {
        resources = getResources();
        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            loginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }
        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
        loginSuccessAction = new LoginSuccessAction();
        socialLoginSuccessAction = new SocialLoginSuccessAction();
        failAction = new FailAction();
    }

    protected void facebookConnect() {
        loginType = LoginType.FACEBOOK;

        if (!appSharedHelper.isShownUserAgreement()) {
            UserAgreementDialogFragment
                    .show(getSupportFragmentManager(), new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    super.onPositive(dialog);
                                    appSharedHelper.saveShownUserAgreement(true);
                                    loginWithFacebook();
                                }
                            });
        } else {
            loginWithFacebook();
        }
    }

    private void loginWithFacebook() {
        AppSession.getSession().closeAndClear();
        DataManager.getInstance().clearAllTables();
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        FacebookHelper.logout(); // clearing old data
        facebookHelper.loginWithFacebook();
    }

    protected void startMainActivity(QBUser user) {
        AppSession.getSession().updateUser(user);
        startMainActivity();
    }

    protected void startMainActivity(boolean importInitialized) {
        appSharedHelper.saveUsersImportInitialized(importInitialized);
        startMainActivity();
    }

    protected void startMainActivity() {
        MainActivity.start(BaseAuthActivity.this);
        finish();
    }

    protected void parseExceptionMessage(Exception exception) {
        hideProgress();

        String errorMessage = exception.getMessage();

        if (errorMessage != null) {
            if (errorMessage.equals(getString(R.string.error_bad_timestamp))) {
                errorMessage = getString(R.string.error_bad_timestamp_from_app);
            } else if (errorMessage.equals(getString(R.string.error_login_or_email_required))) {
                errorMessage = getString(R.string.error_login_or_email_required_from_app);
            } else if (errorMessage.equals(getString(R.string.error_email_already_taken))
                    && loginType.equals(LoginType.FACEBOOK)) {
                errorMessage = getString(R.string.error_email_already_taken_from_app);
            } else if (errorMessage.equals(getString(R.string.error_unauthorized))) {
                errorMessage = getString(R.string.error_unauthorized_from_app);
            }

            ErrorUtils.showError(this, errorMessage);
        }
    }

    protected void parseFailException(Bundle bundle) {
        Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
        int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
        parseExceptionMessage(exception);
    }

    protected void login(String userEmail, String userPassword) {
        appSharedHelper.saveFirstAuth(true);
        appSharedHelper.saveSavedRememberMe(true);
        appSharedHelper.saveUsersImportInitialized(true);
        QBUser user = new QBUser(null, userPassword, userEmail);
        AppSession.getSession().closeAndClear();
        QBLoginCompositeCommand.start(this, user);
    }

    protected void performLoginSuccessAction(Bundle bundle) {
        QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
        startMainActivity(user);

        // send analytics data
        GoogleAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this, user, "User Sign In");
        FlurryAnalyticsHelper.pushAnalyticsData(BaseAuthActivity.this);
    }

    protected boolean isLoggedInToServer() {
        return AppSession.getSession().isLoggedIn();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, loginSuccessAction);
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION, socialLoginSuccessAction);
        addAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION, failAction);

        addAction(QBServiceConsts.SIGNUP_FAIL_ACTION, failAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGIN_FAIL_ACTION);

        removeAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);

        removeAction(QBServiceConsts.SIGNUP_FAIL_ACTION);

        updateBroadcastActionList();
    }

    protected void startLandingScreen() {
        LandingActivity.start(this);
        finish();
    }

    private class LoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            performLoginSuccessAction(bundle);
        }
    }

    private class SocialLoginSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
            QBUpdateUserCommand.start(BaseAuthActivity.this, user, null);

            performLoginSuccessAction(bundle);
        }
    }

    private class FailAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            parseFailException(bundle);
        }
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.d(TAG, "+++ FB StatusCallback call +++");
            if (session.isOpened() && loginType.equals(LoginType.FACEBOOK)) {
                Log.d(TAG, "+++ FB StatusCallback - GO TO LOG IN +++");

                QBSocialLoginCommand.start(BaseAuthActivity.this, QBProvider.FACEBOOK, session.getAccessToken(), null);

                if (BaseAuthActivity.this instanceof SplashActivity) {
                    hideProgress();
                } else {
                    showProgress();
                }

            } else {
                Log.d(TAG, "+++ FB StatusCallback - SESSION CLOSED +++");
                hideProgress();
            }
        }
    }
}