package com.quickblox.q_municate.ui.authorization;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.auth.model.QBProvider;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.ui.base.BaseActivity;
import com.quickblox.q_municate.ui.main.MainActivity;
import com.quickblox.q_municate.utils.AnalyticsUtils;
import com.quickblox.q_municate.utils.FacebookHelper;
import com.quickblox.q_municate.utils.ValidationUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.db.managers.ChatDatabaseManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.LoginType;
import com.quickblox.q_municate_core.qb.commands.QBLoginCompositeCommand;
import com.quickblox.q_municate_core.qb.commands.QBSocialLoginCommand;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.DialogUtils;
import com.quickblox.q_municate_core.utils.PrefsHelper;
import com.quickblox.users.model.QBUser;

public class BaseAuthActivity extends BaseActivity {

    protected static final String STARTED_LOGIN_TYPE = "started_login_type";

    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected FacebookHelper facebookHelper;
    protected LoginType startedLoginType = LoginType.EMAIL;
    protected ValidationUtils validationUtils;
    protected Resources resources;
    protected boolean checkedRememberMe;

    protected UserAgreementDialog userAgreementDialog;

    protected DialogInterface.OnClickListener positiveUserAgreementOnClickListener;
    protected DialogInterface.OnClickListener negativeUserAgreementOnClickListener;

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

        resources = getResources();

        if (savedInstanceState != null && savedInstanceState.containsKey(STARTED_LOGIN_TYPE)) {
            startedLoginType = (LoginType) savedInstanceState.getSerializable(STARTED_LOGIN_TYPE);
        }

        facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());

        loginSuccessAction = new LoginSuccessAction();
        socialLoginSuccessAction = new SocialLoginSuccessAction();
        failAction = new FailAction();
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
    protected void onFailAction(String action) {
        super.onFailAction(action);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STARTED_LOGIN_TYPE, startedLoginType);
        facebookHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void connectFacebookOnClickListener(View view) {
        startedLoginType = LoginType.FACEBOOK;
        if (!isUserAgreementShown()) {
            positiveUserAgreementOnClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveUserAgreementShowing();
                    loginWithFacebook();
                }
            };
            showUserAgreement(positiveUserAgreementOnClickListener, negativeUserAgreementOnClickListener);
        } else {
            loginWithFacebook();
        }
    }

    private void loginWithFacebook() {
        FacebookHelper.logout(); // clearing old data
        facebookHelper.loginWithFacebook();
    }

    protected void showUserAgreement(DialogInterface.OnClickListener positiveClickListener,
            DialogInterface.OnClickListener negativeClickListener) {
        if (userAgreementDialog == null) {
            userAgreementDialog = UserAgreementDialog.newInstance(positiveClickListener,
                    negativeClickListener);
        }
        userAgreementDialog.show(getFragmentManager(), null);
    }

    protected void startMainActivity(Context context, QBUser user, boolean saveRememberMe) {
        ChatDatabaseManager.clearAllCache(context);
        AppSession.getSession().updateUser(user);
        AppSession.saveRememberMe(saveRememberMe);
        MainActivity.start(context);

        finish();
    }

    protected void startMainActivity() {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        MainActivity.start(BaseAuthActivity.this);

        finish();
    }

    protected void saveUserAgreementShowing() {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        prefsHelper.savePref(PrefsHelper.PREF_USER_AGREEMENT, true);
    }

    protected boolean isUserAgreementShown() {
        PrefsHelper prefsHelper = PrefsHelper.getPrefsHelper();
        return prefsHelper.getPref(PrefsHelper.PREF_USER_AGREEMENT, false);
    }

    protected LoginType getCurrentLoginType() {
        return AppSession.getSession().getLoginType();
    }

    protected void parseExceptionMessage(Exception exception) {
        String errorMessage = exception.getMessage();

        hideProgress();

        // TODO: temp decision
        if (exception.getMessage().equals(resources.getString(R.string.error_bad_timestamp))) {
            errorMessage = resources.getString(R.string.error_bad_timestamp_from_app);
        } else if (exception.getMessage().equals(resources.getString(
                R.string.error_email_already_taken)) && startedLoginType.equals(LoginType.FACEBOOK)) {
            errorMessage = resources.getString(R.string.error_email_already_taken_from_app);
            DialogUtils.showLong(BaseAuthActivity.this, errorMessage);
            return;
        }

        validationUtils.setError(errorMessage);
    }

    protected void parseFailException(Bundle bundle) {
        Exception exception = (Exception) bundle.getSerializable(QBServiceConsts.EXTRA_ERROR);
        int errorCode = bundle.getInt(QBServiceConsts.EXTRA_ERROR_CODE);
        parseExceptionMessage(exception);
    }

    protected void login(String userEmail, String userPassword) {
        PrefsHelper.getPrefsHelper().savePref(PrefsHelper.PREF_IMPORT_INITIALIZED, true);
        QBUser user = new QBUser(null, userPassword, userEmail);
        AppSession.getSession().closeAndClear();
        QBLoginCompositeCommand.start(this, user);
    }

    protected void performLoginSuccessAction(Bundle bundle) {
        QBUser user = (QBUser) bundle.getSerializable(QBServiceConsts.EXTRA_USER);
        startMainActivity(BaseAuthActivity.this, user, checkedRememberMe);

        // send analytics data
        AnalyticsUtils.pushAnalyticsData(BaseAuthActivity.this, user, "User Sign In");
    }

    protected boolean isLoggedInToServer() {
        return AppSession.getSession().isLoggedIn();
    }

    private void addActions() {
        addAction(QBServiceConsts.LOGIN_SUCCESS_ACTION, loginSuccessAction);
        addAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION, socialLoginSuccessAction);
        addAction(QBServiceConsts.LOGIN_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION, failAction);
        addAction(QBServiceConsts.SIGNUP_FAIL_ACTION, failAction);
        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.SOCIAL_LOGIN_SUCCESS_ACTION);
        removeAction(QBServiceConsts.LOGIN_FAIL_ACTION);
        removeAction(QBServiceConsts.SOCIAL_LOGIN_FAIL_ACTION);
        removeAction(QBServiceConsts.SIGNUP_FAIL_ACTION);
        updateBroadcastActionList();
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
            checkedRememberMe = true;
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
            if (session.isOpened()) {
                showProgress();
                AppSession.getSession().closeAndClear();
                QBSocialLoginCommand.start(BaseAuthActivity.this, QBProvider.FACEBOOK,
                        session.getAccessToken(), null);
            }
        }
    }
}